package ru.bikbulatov.coroutines_3.data

import android.util.Log
import ru.bikbulatov.coroutines_3.MockBackendLogic

class ProfileApi(
    private val mockBackendLogic: MockBackendLogic = MockBackendLogic()
) {
    suspend fun loadHeader(): String? {
        val token = TokenStorage.token
        Log.d("myAppLogs", "[API] loadHeader with token='$token'")
        val response = mockBackendLogic.loadHeader(token)
        if (response == "403 error") {
            Log.d("myAppLogs", "[API] loadHeader got 403, refreshing token...")
            val newToken = refreshToken(token)
            val retryResponse = mockBackendLogic.loadHeader(newToken)
            return if (retryResponse == "403 error") null else retryResponse
        } else {
            return response
        }
    }

    suspend fun loadBody(): String? {
        val token = TokenStorage.token
        Log.d("myAppLogs", "[API] loadBody with token='$token'")
        val response = mockBackendLogic.loadBody(token)
        if (response == "403 error") {
            Log.d("myAppLogs", "[API] loadBody got 403, refreshing token...")
            val newToken = refreshToken(token)
            val retryResponse = mockBackendLogic.loadBody(newToken)
            return if (retryResponse == "403 error") null else retryResponse
        } else {
            return response
        }
    }

    suspend fun loadFooter(): String? {
        val token = TokenStorage.token
        Log.d("myAppLogs", "[API] loadFooter with token='$token'")
        val response = mockBackendLogic.loadFooter(token)
        if (response == "403 error") {
            Log.d("myAppLogs", "[API] loadFooter got 403, refreshing token...")
            val newToken = refreshToken(token)
            val retryResponse = mockBackendLogic.loadFooter(newToken)
            return if (retryResponse == "403 error") null else retryResponse
        } else {
            return response
        }
    }

    suspend fun refreshToken(token: String): String {
        return mockBackendLogic.refreshToken(token)
    }
}