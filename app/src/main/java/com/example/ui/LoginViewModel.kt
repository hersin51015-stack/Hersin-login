package com.example.ui

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuthRepository
import com.example.data.AppUser
import com.example.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayName: String = "",
    val isCreateAccountMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentUser: AppUser? = null,
    val userProfile: UserProfile? = null
)

class LoginViewModel : ViewModel() {
    private var authRepository: AuthRepository? = null

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun initRepository(context: Context) {
        if (authRepository == null) {
            authRepository = AuthRepository(context.applicationContext)
            val user = authRepository?.getCurrentUser()
            _uiState.update { it.copy(currentUser = user) }
            if (user != null) {
                loadUserProfile(user.uid)
            }
        }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, errorMessage = null) }
    }

    fun updateDisplayName(displayName: String) {
        _uiState.update { it.copy(displayName = displayName, errorMessage = null) }
    }

    fun toggleCreateAccountMode(createMode: Boolean) {
        _uiState.update { it.copy(isCreateAccountMode = createMode, errorMessage = null, successMessage = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun authenticate() {
        val repo = authRepository ?: return
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter email and password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            if (state.isCreateAccountMode) {
                if (state.password != state.confirmPassword) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Passwords do not match.") }
                    return@launch
                }
                val result = repo.signUpWithEmail(state.email.trim(), state.password, state.displayName)
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    _uiState.update { it.copy(isLoading = false, currentUser = user, successMessage = "Account created successfully!") }
                    if (user != null) loadUserProfile(user.uid)
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Sign up failed") }
                }
            } else {
                val result = repo.signInWithEmail(state.email.trim(), state.password)
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    _uiState.update { it.copy(isLoading = false, currentUser = user, successMessage = "Welcome back!") }
                    if (user != null) loadUserProfile(user.uid)
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Sign in failed") }
                }
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        val repo = authRepository ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val webClientId = try {
                    context.getString(com.example.R.string.default_web_client_id)
                } catch (e: Exception) {
                    "YOUR_WEB_CLIENT_ID_PLACEHOLDER"
                }

                if (webClientId == "YOUR_WEB_CLIENT_ID_PLACEHOLDER" || webClientId.isBlank()) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = "Google Sign-In requires a valid web client ID in strings.xml and google-services.json configuration."
                        ) 
                    }
                    return@launch
                }

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId(webClientId)
                    .setFilterByAuthorizedAccounts(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is androidx.credentials.CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val email = googleIdTokenCredential.id
                    val name = googleIdTokenCredential.displayName ?: email.substringBefore("@")
                    
                    val authResult = repo.signInWithGoogleIdToken(idToken, email, name)
                    if (authResult.isSuccess) {
                        val user = authResult.getOrNull()
                        _uiState.update { it.copy(isLoading = false, currentUser = user, successMessage = "Google Sign-In successful!") }
                        if (user != null) loadUserProfile(user.uid)
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = authResult.exceptionOrNull()?.localizedMessage ?: "Google sign in failed") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Unsupported credential type.") }
                }
            } catch (e: androidx.credentials.exceptions.NoCredentialException) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "No Google accounts found on this device emulator. Please use Email & Password below."
                    ) 
                }
            } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Google Sign-In error. Please ensure google-services.json is configured.") }
            }
        }
    }

    fun loadUserProfile(uid: String) {
        val repo = authRepository ?: return
        viewModelScope.launch {
            val result = repo.getUserProfile(uid)
            if (result.isSuccess) {
                _uiState.update { it.copy(userProfile = result.getOrNull()) }
            }
        }
    }

    fun updateBio(newBio: String) {
        val repo = authRepository ?: return
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            val result = repo.updateBio(user.uid, newBio)
            if (result.isSuccess) {
                loadUserProfile(user.uid)
                _uiState.update { it.copy(successMessage = "Profile updated!") }
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to update profile") }
            }
        }
    }

    fun signOut() {
        authRepository?.signOut()
        _uiState.update { 
            LoginUiState(
                currentUser = null, 
                userProfile = null, 
                successMessage = "Signed out successfully"
            ) 
        }
    }
}
