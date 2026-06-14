package ru.bikbulatov.coroutines_3

import android.util.Log
import kotlinx.coroutines.delay

class MockBackendLogic {
    var validToken = "hashedToken"
    
    fun isTokenValid(token: String): Boolean {
        val isValid = token == validToken
        Log.d("myAppLogsBackend", "Token validation: '$token' == '$validToken' → $isValid")
        return isValid
    }

    suspend fun loadHeader(token: String): String {
        Log.d("myAppLogsBackend", "loadHeader called with token: '$token', valid token: '$validToken'")
        delay(100) 
        if (isTokenValid(token)) {
            return "Header info"
        } else {
            return "403 error"
        }
    }

    suspend fun loadBody(token: String): String {
        Log.d("myAppLogsBackend", "loadBody called with token: '$token', valid token: '$validToken'")
        delay(100)
        if (isTokenValid(token)) {
            return "Body info"
        } else {
            return "403 error"
        }
    }

    suspend fun loadFooter(token: String): String {
        Log.d("myAppLogsBackend", "loadFooter called with token: '$token', valid token: '$validToken'")
        delay(100)
        if (isTokenValid(token)) {
            return "Footer info"
        } else {
            return "403 error"
        }
    }

    suspend fun refreshToken(token: String): String {
        Log.d("myAppLogsBackend", "refreshToken called with old token: '$token'")
        delay(50) 
        validToken += "_new"
        Log.d("myAppLogsBackend", "Token updated to: '$validToken'")
        return validToken
    }
}