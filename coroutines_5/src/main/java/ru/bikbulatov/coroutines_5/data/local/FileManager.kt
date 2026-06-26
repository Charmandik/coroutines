package ru.bikbulatov.coroutines_5.data.local

import android.util.Log
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException

class FileManager {

    private var shouldFail = false
    private val cacheDir = "/data/cache"

    fun setShouldFail(shouldFail: Boolean) {
        this.shouldFail = shouldFail
    }

    fun saveCache(data: String) {
        logThread("saveCache")
        
        if (shouldFail) {
            throw IOException("File system error: Unable to write cache")
        }
        Thread.sleep(500)
    }

    suspend fun loadCache(): String {
        logThread("loadCache")
        
        delay(50)
        if (shouldFail) {
            throw IOException("File system error: Unable to read cache")
        }
        Thread.sleep(500)
        return "Cached data"
    }

    suspend fun deleteOldFiles() {
        logThread("deleteOldFiles")
        
        delay(50)
        if (shouldFail) {
            throw IOException("File system error: Unable to delete files")
        }
        Thread.sleep(300)
    }

    private fun logThread(method: String) {
        val threadName = Thread.currentThread().name
        val isMain = threadName.contains("main", ignoreCase = true)
        
        Log.d("DISPATCHER", "$method running on: $threadName")
        Log.d("DISPATCHER", "Is Main? $isMain")
        
        if (isMain) {
            Log.e("BUG", "⚠️ $method BLOCKING MAIN THREAD!")
        } else {
            Log.i("BUG", "✅ $method running on background thread")
        }
    }
}
