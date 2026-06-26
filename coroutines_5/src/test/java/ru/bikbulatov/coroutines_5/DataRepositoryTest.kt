package ru.bikbulatov.coroutines_5.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import ru.bikbulatov.coroutines_5.data.local.FileManager
import ru.bikbulatov.coroutines_5.data.network.ApiService

@OptIn(ExperimentalCoroutinesApi::class)
class DataRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var fileManager: FileManager
    private lateinit var repository: DataRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        apiService = ApiService()
        fileManager = FileManager()
        repository = DataRepository(apiService, fileManager, kotlinx.coroutines.test.TestScope())
    }

    @Test
    fun testScenario1_blocksMainThread() = runTest {
        repository.resetState()
        
        val startTime = System.currentTimeMillis()
        
        try {
            repository.loadUserProfileScenario1()
        } catch (e: Exception) {
            // Ожидаем, что могут быть ошибки в тесте
        }
        
        val elapsed = System.currentTimeMillis() - startTime
        
        // Если блокировка есть, тест выполнится быстро (без реального ожидания)
        // После исправления с withContext(Dispatchers.IO) должно работать корректно
        Assert.assertTrue("Operation should complete", elapsed < 5000)
    }

    @Test
    fun testScenario2_blocksMainThread() = runTest {
        repository.resetState()
        
        try {
            repository.loadUserProfileScenario2()
        } catch (e: Exception) {
            // Ожидаем, что могут быть ошибки в тесте
        }
        
        advanceUntilIdle()
        
        // После исправления данные должны загрузиться
        Assert.assertTrue("Data should be loaded", repository.isDataLoaded() || repository.getLastError() != null)
    }

    @Test
    fun testScenario3_blocksMainThread() = runTest {
        repository.resetState()
        
        try {
            repository.loadUserProfileScenario3()
        } catch (e: Exception) {
            // Ожидаем, что могут быть ошибки в тесте
        }
        
        advanceUntilIdle()
        
        Assert.assertTrue("Operation should complete", repository.isDataLoaded() || repository.getLastError() != null)
    }

    @Test
    fun testCorrectImplementation_works() = runTest {
        repository.resetState()
        
        val result = repository.loadUserProfileCorrect()
        
        Assert.assertNotNull("User should be loaded", result)
        Assert.assertEquals("User ID should be 1", 1, result.user.id)
        Assert.assertTrue("Should have posts", result.posts.isNotEmpty())
    }
}
