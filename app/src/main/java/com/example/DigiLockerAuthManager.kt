package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

class DigiLockerAuthManager(private val context: Context) {

    private val authService = AuthorizationService(context)

    fun getAuthIntent(): Intent {
        // In a real environment, these endpoints would be the actual DigiLocker NAD endpoints
        // such as https://digilocker.meripehchaan.gov.in/public/oauth2/1/authorize
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("https://api.digitallocker.gov.in/public/oauth2/1/authorize"),
            Uri.parse("https://api.digitallocker.gov.in/public/oauth2/1/token")
        )

        val clientId = "ARHAM_MOCK_CLIENT_ID"
        val redirectUri = Uri.parse("arham://oauth2callback")

        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfig,
            clientId,
            ResponseTypeValues.CODE,
            redirectUri
        )

        val authRequest = authRequestBuilder
            .setScope("read_docs")
            .build()

        return authService.getAuthorizationRequestIntent(authRequest)
    }

    fun dispose() {
        authService.dispose()
    }
}
