package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val bio: String = "Welcome to Login App!",
    val createdAt: Long = System.currentTimeMillis()
)

data class AppUser(
    val uid: String,
    val email: String,
    val displayName: String
)

class AuthRepository(private val context: Context? = null) {
    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null
    private var isFirebaseInitialized = false

    init {
        try {
            if (context != null && FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            firebaseAuth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            isFirebaseInitialized = true
        } catch (e: Exception) {
            isFirebaseInitialized = false
        }
    }

    private val prefs: SharedPreferences? = context?.getSharedPreferences("local_auth_prefs", Context.MODE_PRIVATE)

    fun getCurrentUser(): AppUser? {
        if (isFirebaseInitialized && firebaseAuth?.currentUser != null) {
            val fbUser = firebaseAuth!!.currentUser!!
            return AppUser(
                uid = fbUser.uid,
                email = fbUser.email ?: "",
                displayName = fbUser.displayName ?: fbUser.email?.substringBefore("@") ?: "User"
            )
        } else {
            val email = prefs?.getString("logged_in_email", null) ?: return null
            val uid = prefs?.getString("logged_in_uid", "local_uid") ?: "local_uid"
            val name = prefs?.getString("logged_in_name", email.substringBefore("@")) ?: email.substringBefore("@")
            return AppUser(uid = uid, email = email, displayName = name)
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<AppUser> {
        return if (isFirebaseInitialized && firebaseAuth != null) {
            try {
                val result = firebaseAuth!!.signInWithEmailAndPassword(email, pass).await()
                val user = result.user ?: throw Exception("Sign in failed")
                Result.success(
                    AppUser(
                        uid = user.uid,
                        email = user.email ?: email,
                        displayName = user.displayName ?: email.substringBefore("@")
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            try {
                val usersJsonStr = prefs?.getString("users_db", "{}") ?: "{}"
                val usersObj = JSONObject(usersJsonStr)
                if (usersObj.has(email)) {
                    val userObj = usersObj.getJSONObject(email)
                    val storedPass = userObj.getString("password")
                    if (storedPass == pass) {
                        val uid = userObj.getString("uid")
                        val name = userObj.optString("displayName", email.substringBefore("@"))
                        prefs?.edit()
                            ?.putString("logged_in_email", email)
                            ?.putString("logged_in_uid", uid)
                            ?.putString("logged_in_name", name)
                            ?.apply()
                        Result.success(AppUser(uid = uid, email = email, displayName = name))
                    } else {
                        Result.failure(Exception("Incorrect password."))
                    }
                } else {
                    Result.failure(Exception("Account not found. Please create an account."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, displayName: String): Result<AppUser> {
        val name = displayName.ifEmpty { email.substringBefore("@") }
        return if (isFirebaseInitialized && firebaseAuth != null && firestore != null) {
            try {
                val result = firebaseAuth!!.createUserWithEmailAndPassword(email, pass).await()
                val user = result.user ?: throw Exception("Account creation failed")
                
                val profile = UserProfile(
                    uid = user.uid,
                    email = email,
                    displayName = name,
                    bio = "New account created manually"
                )
                firestore!!.collection("users").document(user.uid).set(profile).await()

                Result.success(AppUser(uid = user.uid, email = email, displayName = name))
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            try {
                val usersJsonStr = prefs?.getString("users_db", "{}") ?: "{}"
                val usersObj = JSONObject(usersJsonStr)
                if (usersObj.has(email)) {
                    Result.failure(Exception("Email already exists. Please log in."))
                } else {
                    val uid = "local_uid_${System.currentTimeMillis()}"
                    val userObj = JSONObject().apply {
                        put("uid", uid)
                        put("email", email)
                        put("password", pass)
                        put("displayName", name)
                        put("bio", "New account created locally")
                    }
                    usersObj.put(email, userObj)
                    prefs?.edit()
                        ?.putString("users_db", usersObj.toString())
                        ?.putString("logged_in_email", email)
                        ?.putString("logged_in_uid", uid)
                        ?.putString("logged_in_name", name)
                        ?.apply()
                    Result.success(AppUser(uid = uid, email = email, displayName = name))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun signInWithGoogleIdToken(idToken: String, email: String, displayName: String): Result<AppUser> {
        return if (isFirebaseInitialized && firebaseAuth != null && firestore != null) {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = firebaseAuth!!.signInWithCredential(credential).await()
                val user = result.user ?: throw Exception("Google sign in failed")
                
                val docRef = firestore!!.collection("users").document(user.uid)
                val snapshot = docRef.get().await()
                if (!snapshot.exists()) {
                    val profile = UserProfile(
                        uid = user.uid,
                        email = user.email ?: email,
                        displayName = user.displayName ?: displayName,
                        bio = "Signed in with Google"
                    )
                    docRef.set(profile).await()
                }

                Result.success(
                    AppUser(
                        uid = user.uid,
                        email = user.email ?: email,
                        displayName = user.displayName ?: displayName
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("Firebase is not initialized. Please configure google-services.json."))
        }
    }

    suspend fun getUserProfile(uid: String): Result<UserProfile> {
        return if (isFirebaseInitialized && firestore != null) {
            try {
                val doc = firestore!!.collection("users").document(uid).get().await()
                val profile = doc.toObject(UserProfile::class.java) ?: UserProfile(uid = uid)
                Result.success(profile)
            } catch (e: Exception) {
                getLocalProfile(uid)
            }
        } else {
            getLocalProfile(uid)
        }
    }

    private fun getLocalProfile(uid: String): Result<UserProfile> {
        try {
            val email = prefs?.getString("logged_in_email", "user@example.com") ?: "user@example.com"
            val name = prefs?.getString("logged_in_name", email.substringBefore("@")) ?: email.substringBefore("@")
            val usersJsonStr = prefs?.getString("users_db", "{}") ?: "{}"
            val usersObj = JSONObject(usersJsonStr)
            if (usersObj.has(email)) {
                val userObj = usersObj.getJSONObject(email)
                val profile = UserProfile(
                    uid = userObj.optString("uid", uid),
                    email = userObj.optString("email", email),
                    displayName = userObj.optString("displayName", name),
                    bio = userObj.optString("bio", "Welcome to Login App!")
                )
                return Result.success(profile)
            }
            return Result.success(UserProfile(uid = uid, email = email, displayName = name))
        } catch (e: Exception) {
            return Result.success(UserProfile(uid = uid))
        }
    }

    suspend fun updateBio(uid: String, bio: String): Result<Unit> {
        return if (isFirebaseInitialized && firestore != null) {
            try {
                firestore!!.collection("users").document(uid).update("bio", bio).await()
                Result.success(Unit)
            } catch (e: Exception) {
                updateLocalBio(bio)
            }
        } else {
            updateLocalBio(bio)
        }
    }

    private fun updateLocalBio(bio: String): Result<Unit> {
        try {
            val email = prefs?.getString("logged_in_email", null) ?: return Result.failure(Exception("Not logged in"))
            val usersJsonStr = prefs?.getString("users_db", "{}") ?: "{}"
            val usersObj = JSONObject(usersJsonStr)
            if (usersObj.has(email)) {
                val userObj = usersObj.getJSONObject(email)
                userObj.put("bio", bio)
                usersObj.put(email, userObj)
                prefs?.edit()?.putString("users_db", usersObj.toString())?.apply()
            }
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    fun signOut() {
        if (isFirebaseInitialized) {
            try {
                firebaseAuth?.signOut()
            } catch (e: Exception) {}
        }
        prefs?.edit()
            ?.remove("logged_in_email")
            ?.remove("logged_in_uid")
            ?.remove("logged_in_name")
            ?.apply()
    }
}
