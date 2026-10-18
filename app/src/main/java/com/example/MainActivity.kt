package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.auth.JulianAuthScreen
import com.example.ui.auth.signOutJulianSession
import com.example.ui.screens.JulianOmniCoreScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.JulianViewModel
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before any UI or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            JulianAppRoot()
        }
    }
}

@Composable
fun JulianAppRoot(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (user == null) {
        MyApplicationTheme(darkTheme = true) {
            JulianAuthScreen(
                onAuthSuccess = { /* AuthStateListener automatically updates currentUser */ }
            )
        }
    } else {
        val julianViewModel: JulianViewModel = viewModel(
            key = user.uid,
            factory = JulianViewModel.provideFactory(user.uid)
        )
        val isDarkTheme by julianViewModel.isDarkTheme.collectAsStateWithLifecycle()

        MyApplicationTheme(darkTheme = isDarkTheme) {
            JulianOmniCoreScreen(
                viewModel = julianViewModel,
                onSignOut = {
                    signOutJulianSession(
                        context = context,
                        onSignOutComplete = {},
                        scope = scope
                    )
                }
            )
        }
    }
}
