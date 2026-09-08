package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DashboardScreen
import com.example.ui.LoginScreen
import com.example.ui.LoginViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: LoginViewModel = viewModel()
        val context = androidx.compose.ui.platform.LocalContext.current
        
        LaunchedEffect(Unit) {
          viewModel.initRepository(context)
        }

        val state by viewModel.uiState.collectAsState()

        val currentUser = state.currentUser
        if (currentUser != null) {
          DashboardScreen(
            user = currentUser,
            userProfile = state.userProfile,
            onUpdateBio = { viewModel.updateBio(it) },
            onSignOut = { viewModel.signOut() }
          )
        } else {
          LoginScreen(
            state = state,
            onEmailChange = { viewModel.updateEmail(it) },
            onPasswordChange = { viewModel.updatePassword(it) },
            onConfirmPasswordChange = { viewModel.updateConfirmPassword(it) },
            onDisplayNameChange = { viewModel.updateDisplayName(it) },
            onToggleMode = { viewModel.toggleCreateAccountMode(it) },
            onSubmit = { viewModel.authenticate() },
            onGoogleSignIn = { ctx -> viewModel.signInWithGoogle(ctx) },
            onClearMessages = { viewModel.clearMessages() }
          )
        }
      }
    }
  }
}
