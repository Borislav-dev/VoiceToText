package org.example.project.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object LoginRoute

@Serializable
data object HomeRoute

@Serializable
data object RecordingRoute

@Serializable
data class NoteDetailsRoute(val noteId: String)

@Serializable
data object SettingsRoute

@Serializable
data object PaywallRoute

@Serializable
data object OnboardingRoute
