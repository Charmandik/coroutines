package ru.bikbulatov.coroutines_3

import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import ru.bikbulatov.coroutines_3.data.ProfileApi
import ru.bikbulatov.coroutines_3.data.TokenStorage

class RaceConditionDemoTest {

    @Test
    fun `demonstrate race condition with token refresh`() = runBlocking {
        val mockBackend = MockBackendLogic()
        mockBackend.validToken = "old_HashedToken"
        TokenStorage.token = "old_HashedToken"
        
        val api = ProfileApi(mockBackend)
        
        Log.d("myAppLogs", "\n=== RACE CONDITION DEMO ===")
        Log.d("myAppLogs", "Initial token: '${TokenStorage.token}'")
        Log.d("myAppLogs", "Backend valid token: '${mockBackend.validToken}'")
        
        coroutineScope {
            val headerJob = async {
                Log.d("myAppLogs", "[Header] Starting request with token: '${TokenStorage.token}'")
                api.loadHeader()
            }
            
            val bodyJob = async {
                Log.d("myAppLogs", "[Body] Starting request with token: '${TokenStorage.token}'")
                api.loadBody()
            }
            
            val footerJob = async {
                Log.d("myAppLogs", "[Footer] Starting request with token: '${TokenStorage.token}'")
                api.loadFooter()
            }
            
            val headerResult = headerJob.await()
            val bodyResult = bodyJob.await()
            val footerResult = footerJob.await()
            
            Log.d("myAppLogs", "")
            Log.d("myAppLogs", "=== RESULTS ===")
            Log.d("myAppLogs", "Header: $headerResult")
            Log.d("myAppLogs", "Body: $bodyResult")
            Log.d("myAppLogs", "Footer: $footerResult")
            Log.d("myAppLogs", "")
            Log.d("myAppLogs", "Final token: '${TokenStorage.token}'")
            Log.d("myAppLogs", "Final backend valid token: '${mockBackend.validToken}'")
            
            if (headerResult == null || bodyResult == null || footerResult == null) {
                Log.d("myAppLogs", "")
                Log.d("myAppLogs", "❌ RACE CONDITION DETECTED!")
                Log.d("myAppLogs", "Some requests failed because token was refreshed multiple times")
            } else {
                Log.d("myAppLogs", "")
                Log.d("myAppLogs", "✅ All requests succeeded (race condition didn't occur this time)")
            }
        }
    }
}
