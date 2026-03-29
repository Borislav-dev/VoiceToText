package org.example.project.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

private const val WEB_CLIENT_ID = "750938353574-kbtt5169f30uftrf027a6iinnvrv3qfg.apps.googleusercontent.com"

actual suspend fun getGoogleIdToken(activityContext: Any?): String {
    val context = activityContext as Context

    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(WEB_CLIENT_ID)
        .setFilterByAuthorizedAccounts(false)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    val credentialManager = CredentialManager.create(context)
    val result = credentialManager.getCredential(context, request)

    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
    return googleIdTokenCredential.idToken
}
