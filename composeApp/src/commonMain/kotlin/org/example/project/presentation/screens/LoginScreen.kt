package org.example.project.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import org.example.project.auth.getPlatformContext
import org.example.project.presentation.viewmodels.AuthViewModel
import org.example.project.supabaseClient
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import voicetotext.composeapp.generated.resources.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val platformContext = getPlatformContext()

    // Reactively observe Supabase session status.
    // When Authenticated (either from native sign-in or existing session), navigate away.
    LaunchedEffect(Unit) {
        supabaseClient.auth.sessionStatus.collect { status ->
            if (status is SessionStatus.Authenticated) {
                onLoginSuccess()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                // Spacer to push content slightly up from exact center for better visual balance
                Spacer(modifier = Modifier.weight(1f))

                // App Title / Logo area
                Text(
                    text = stringResource(Res.string.login_title),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Welcome Message
                Text(
                    text = "Welcome back! Please sign in to continue building your notes with AI.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Google Sign In Button matching the requested design
                Button(
                    onClick = { viewModel.onGoogleSignInClicked(platformContext) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF131314),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_google),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp).padding(end = 8.dp),
                        tint = Color.Unspecified // Retain the original colors of the vector
                    )
                    Text(
                        text = "Sign in with Google",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                // Error message area
                Box(modifier = Modifier.height(64.dp).padding(top = 16.dp), contentAlignment = Alignment.TopCenter) {
                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Bottom spacer
                Spacer(modifier = Modifier.weight(1.5f))
            }
        }
    }
}
