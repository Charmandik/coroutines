package ru.bikbulatov.coroutines_5.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.bikbulatov.coroutines_5.data.local.FileManager
import ru.bikbulatov.coroutines_5.data.network.ApiService
import ru.bikbulatov.coroutines_5.data.repository.DataRepository
import ru.bikbulatov.coroutines_5.presentation.model.PostVO
import ru.bikbulatov.coroutines_5.presentation.model.UserProfileVO
import ru.bikbulatov.coroutines_5.presentation.model.UserVO

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val profile: UserProfileVO) : UiState()
    data class Error(val message: String, val scenario: String) : UiState()
}

class DataViewModel : ViewModel() {

    private val apiService = ApiService()
    private val fileManager = FileManager()
    private val repository = DataRepository(apiService, fileManager, viewModelScope)

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadScenario1() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.resetState()
            apiService.setShouldFail(false)
            fileManager.setShouldFail(false)

            try {
                val result = repository.loadUserProfileScenario1()
                _uiState.value = UiState.Success(UserProfileVO.fromDomain(result))
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Unknown error",
                    scenario = "Scenario 1: File I/O on Main thread"
                )
            }
        }
    }

    fun loadScenario2() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.resetState()
            apiService.setShouldFail(false)
            fileManager.setShouldFail(false)

            try {
                val result = repository.loadUserProfileScenario2()
                _uiState.value = UiState.Success(UserProfileVO.fromDomain(result))
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Unknown error",
                    scenario = "Scenario 2: Network + Cache on Main thread"
                )
            }
        }
    }

    fun loadScenario3() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.resetState()
            apiService.setShouldFail(false)
            fileManager.setShouldFail(false)

            try {
                val result = repository.loadUserProfileScenario3()
                _uiState.value = UiState.Success(UserProfileVO.fromDomain(result))
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Unknown error",
                    scenario = "Scenario 3: CPU-heavy on Main thread"
                )
            }
        }
    }

    fun loadCorrect() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.resetState()
            apiService.setShouldFail(false)
            fileManager.setShouldFail(false)

            try {
                val result = repository.loadUserProfileCorrect()
                _uiState.value = UiState.Success(UserProfileVO.fromDomain(result))
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Unknown error",
                    scenario = "Correct implementation"
                )
            }
        }
    }

    fun reset() {
        repository.resetState()
        _uiState.value = UiState.Idle
    }
}
