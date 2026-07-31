package com.example

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class FirebaseAuthManager(private val context: Context) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    suspend fun signInWithGoogle(): Boolean {
        return try {
            val credentialManager = CredentialManager.create(context)
            
            // Note: Replace with actual Web Client ID from Firebase Console / Google Cloud
            // In a real app, you get this from BuildConfig or google-services.json
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            // Using the Web Client ID from BuildConfig (configured via .env)
            val webClientId = BuildConfig.WEB_CLIENT_ID

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            
            if (credential is GoogleIdTokenCredential) {
                val idToken = credential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential).await()
                true
            } else {
                Log.e("Auth", "Unexpected credential type")
                false
            }
        } catch (e: Exception) {
            Log.e("Auth", "Sign in failed", e)
            false
        }
    }
    
    fun signOut() {
        auth.signOut()
    }
}
