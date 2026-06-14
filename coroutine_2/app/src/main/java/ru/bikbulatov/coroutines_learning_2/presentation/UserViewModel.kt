package ru.bikbulatov.coroutines_learning_2.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.bikbulatov.coroutines_learning_2.data.network.ApiService
import ru.bikbulatov.coroutines_learning_2.data.repository.UserRepository
import ru.bikbulatov.coroutines_learning_2.presentation.model.PostVO
import ru.bikbulatov.coroutines_learning_2.presentation.model.SettingsVO
import ru.bikbulatov.coroutines_learning_2.presentation.model.UserVO

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(
        val user: UserVO,
        val posts: List<PostVO>,
        val settings: SettingsVO
    ) : UiState()
    data class Error(val message: String, val scenario: String) : UiState()
}

class UserViewModel : ViewModel() {

    private val apiService = ApiService()
    private val repository = UserRepository(apiService, viewModelScope)

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadUserDataScenario1() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.resetState()
            apiService.setFailUser(true)

            try {
                val result = repository.getUserDataWithCatch()
                _uiState.value = UiState.Success(
                    UserVO.fromDomain(result.first),
                    result.second.map { PostVO.fromDomain(it) },
                    SettingsVO.fromDomain(result.third)
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Unknown error",
                    scenario = "Scenario 1: try/catch around launch"
                )
            }
        }
    }

    fun loadUserDataScenario2() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.resetState()
            apiService.setFailUser(true)

            try {
                val result = repository.getUserDataWithNestedLaunch()
                _uiState.value = UiState.Success(
                    UserVO.fromDomain(result.first),
                    result.second.map { PostVO.fromDomain(it) },
                    SettingsVO.fromDomain(result.third)
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Unknown error",
                    scenario = "Scenario 2: try/catch in nested launch"
                )
            }
        }
    }

    fun loadUserDataScenario3() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.resetState()
            apiService.setFailUser(true)

            try {
                val result = repository.getUserDataWithAsync()
                _uiState.value = UiState.Success(
                    UserVO.fromDomain(result.first),
                    result.second.map { PostVO.fromDomain(it) },
                    SettingsVO.fromDomain(result.third)
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Unknown error",
                    scenario = "Scenario 3: async without proper await"
                )
            }
        }
    }

    fun loadUserDataCorrect() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.resetState()
            apiService.setFailUser(false)

            try {
                val result = repository.getUserDataCorrect()
                _uiState.value = UiState.Success(
                    UserVO.fromDomain(result.first),
                    result.second.map { PostVO.fromDomain(it) },
                    SettingsVO.fromDomain(result.third)
                )
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
        apiService.setFailUser(false)
        apiService.setFailPosts(false)
        apiService.setFailSettings(false)
        _uiState.value = UiState.Idle
    }
}
