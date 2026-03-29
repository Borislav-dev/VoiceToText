package org.example.project.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import org.example.project.data.audio.AudioPlayer
import org.example.project.data.audio.AudioRecorder
import org.example.project.data.repository.DeepgramTranscriptionRepositoryImpl
import org.example.project.data.repository.ProfileRepositoryImpl
import com.russhwolf.settings.Settings
import org.example.project.data.repository.PreferencesRepositoryImpl
import org.example.project.data.repository.SubscriptionRepositoryImpl
import org.example.project.data.repository.SupabaseNotesRepositoryImpl
import org.example.project.domain.audio.IAudioPlayer
import org.example.project.domain.repository.INotesRepository
import org.example.project.domain.repository.IPreferencesRepository
import org.example.project.domain.repository.IProfileRepository
import org.example.project.domain.repository.ISubscriptionRepository
import org.example.project.domain.repository.ITranscriptionRepository
import org.example.project.domain.share.IShareManager
import org.example.project.domain.share.ShareManager
import org.example.project.domain.repository.IAuthRepository
import org.example.project.data.repository.SupabaseAuthRepositoryImpl
import org.example.project.presentation.viewmodels.AuthViewModel
import org.example.project.presentation.viewmodels.HomeViewModel
import org.example.project.presentation.viewmodels.NoteDetailsViewModel
import org.example.project.presentation.viewmodels.OnboardingViewModel
import org.example.project.presentation.viewmodels.PaywallViewModel
import org.example.project.presentation.viewmodels.RecordingViewModel
import org.example.project.presentation.viewmodels.SettingsViewModel
import org.example.project.supabaseClient
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.example.project.auth.getPlatformContext

val appModule = module {
    // Core Dependencies
    single { supabaseClient }
    single { 
        HttpClient { 
            install(HttpTimeout) { 
                requestTimeoutMillis = 300_000L
                connectTimeoutMillis = 300_000L
                socketTimeoutMillis = 300_000L 
            } 
        } 
    }

    // Storage
    single { Settings() }

    // Repositories (Singletons)
    single<IAuthRepository> { SupabaseAuthRepositoryImpl(get()) }
    single<IPreferencesRepository> { PreferencesRepositoryImpl(get()) }
    single<INotesRepository> { SupabaseNotesRepositoryImpl(get()) }
    single<IProfileRepository> { ProfileRepositoryImpl(get()) }
    single<ISubscriptionRepository> { SubscriptionRepositoryImpl(get()) }
    single<ITranscriptionRepository> { DeepgramTranscriptionRepositoryImpl(get()) }

    // ViewModels
    viewModel { AuthViewModel(get(), get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { parameters -> 
        RecordingViewModel(
            audioRecorder = AudioRecorder(parameters.get()), 
            transcriptionRepository = get(), 
            notesRepository = get(), 
            profileRepository = get(), 
            subscriptionRepository = get()
        ) 
    }
    viewModel { parameters -> 
        NoteDetailsViewModel(
            noteId = parameters[0], 
            notesRepository = get(), 
            transcriptionRepository = get(),
            audioPlayer = AudioPlayer(parameters[1]),
            shareManager = ShareManager(parameters[1])
        ) 
    }
    viewModel { SettingsViewModel(get()) }
    viewModel { PaywallViewModel() }
    viewModel { OnboardingViewModel(get()) }
}
