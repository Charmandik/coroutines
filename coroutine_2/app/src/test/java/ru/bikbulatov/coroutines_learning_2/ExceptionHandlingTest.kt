package ru.bikbulatov.coroutines_learning_2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import ru.bikbulatov.coroutines_learning_2.data.network.ApiService
import ru.bikbulatov.coroutines_learning_2.data.repository.UserRepository
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ExceptionHandlingTest {

    @Test
    fun scenario1_tryCatchAroundLaunch_doesNotCatchException() = runTest {
        val testDispatcher = StandardTestDispatcher()
        val testScope = CoroutineScope(testDispatcher)
        val apiService = ApiService()
        val repository = UserRepository(apiService, testScope)
        
        apiService.setFailUser(true)
        repository.resetState()

        var exceptionThrown: Throwable? = null

        try {
            repository.getUserDataWithCatch()
        } catch (e: Exception) {
            exceptionThrown = e
        }

        advanceUntilIdle()

        assertNotNull("Exception should be thrown from repository method", exceptionThrown)
        assertTrue("Exception should be IOException", exceptionThrown is IOException)
        assertFalse("userDataLoaded should be false", repository.isUserDataLoaded())
    }

    @Test
    fun scenario2_nestedLaunch_doesNotCatchExceptionInParent() = runTest {
        val testDispatcher = StandardTestDispatcher()
        val testScope = CoroutineScope(testDispatcher)
        val apiService = ApiService()
        val repository = UserRepository(apiService, testScope)
        
        apiService.setFailUser(true)
        repository.resetState()

        var exceptionThrown: Throwable? = null

        try {
            repository.getUserDataWithNestedLaunch()
        } catch (e: Exception) {
            exceptionThrown = e
        }

        advanceUntilIdle()

        assertNotNull("Exception should be thrown from repository method", exceptionThrown)
        assertTrue("Exception should be IOException", exceptionThrown is IOException)
        assertFalse("userDataLoaded should be false", repository.isUserDataLoaded())
    }

    @Test
    fun scenario3_asyncWithoutProperAwait_propagatesException() = runTest {
        val testDispatcher = StandardTestDispatcher()
        val testScope = CoroutineScope(testDispatcher)
        val apiService = ApiService()
        val repository = UserRepository(apiService, testScope)
        
        apiService.setFailUser(true)
        repository.resetState()

        var exceptionThrown: Throwable? = null

        try {
            repository.getUserDataWithAsync()
        } catch (e: Exception) {
            exceptionThrown = e
        }

        advanceUntilIdle()

        assertNotNull("Exception should be thrown from async block", exceptionThrown)
        assertTrue("Exception should be IOException", exceptionThrown is IOException)
        assertFalse("userDataLoaded should be false", repository.isUserDataLoaded())
    }

    @Test
    fun correctImplementation_handlesExceptionProperly() = runTest {
        val testDispatcher = StandardTestDispatcher()
        val testScope = CoroutineScope(testDispatcher)
        val apiService = ApiService()
        val repository = UserRepository(apiService, testScope)
        
        apiService.setFailUser(true)
        repository.resetState()

        var exceptionThrown: Throwable? = null

        try {
            repository.getUserDataCorrect()
        } catch (e: Exception) {
            exceptionThrown = e
        }

        assertNotNull("Exception should be thrown and caught", exceptionThrown)
        assertTrue("Exception should be IOException", exceptionThrown is IOException)
    }

    @Test
    fun correctImplementation_successWhenNoError() = runTest {
        val testDispatcher = StandardTestDispatcher()
        val testScope = CoroutineScope(testDispatcher)
        val apiService = ApiService()
        val repository = UserRepository(apiService, testScope)
        
        apiService.setFailUser(false)
        apiService.setFailPosts(false)
        apiService.setFailSettings(false)
        repository.resetState()

        var result: Triple<*, *, *>? = null
        var exceptionThrown: Throwable? = null

        try {
            result = repository.getUserDataCorrect()
        } catch (e: Exception) {
            exceptionThrown = e
        }

        advanceUntilIdle()

        assertNull("No exception should be thrown", exceptionThrown)
        assertNotNull("Result should be returned", result)
        assertTrue("userDataLoaded should be true", repository.isUserDataLoaded())
        assertTrue("postsLoaded should be true", repository.isPostsLoaded())
        assertTrue("settingsLoaded should be true", repository.isSettingsLoaded())
    }
}
