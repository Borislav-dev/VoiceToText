package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.di.appModule
import org.example.project.presentation.navigation.AppNavigation
import org.example.project.theme.AppTheme
import org.koin.compose.KoinApplication

import org.koin.compose.KoinContext
import org.koin.core.context.GlobalContext

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        AppTheme {
            AppNavigation()
        }
    }
}
