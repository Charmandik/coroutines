package ru.bikbulatov.coroutines_3.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.bikbulatov.coroutines_3.MockBackendLogic
import ru.bikbulatov.coroutines_3.data.ProfileApi
import ru.bikbulatov.coroutines_3.data.ProfileRepository
import ru.bikbulatov.coroutines_3.data.TokenStorage
import ru.bikbulatov.coroutines_3.domain.ProfileInteractor

class ProfileViewModel(
    private val mockBackendLogic: MockBackendLogic = MockBackendLogic(),
    private val profileInteractor: ProfileInteractor = ProfileInteractor(
        ProfileRepository(ProfileApi(mockBackendLogic))
    )
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private fun addLog(message: String) {
        Log.d("myAppLogs", message)
        val currentLogs = _logs.value.toMutableList()
        currentLogs.add(message)
        _logs.value = currentLogs
    }

    fun loadProfile() {
        viewModelScope.launch {
            _logs.value = emptyList()
            _state.value = ProfileUiState.Loading
            
            addLog("=== Starting 3 parallel requests ===")
            addLog("Initial token: '${TokenStorage.token}'")
            addLog("Backend valid token: '${mockBackendLogic.validToken}'")

            val headerResult = async {
                addLog("[Header] Request started")
                val result = profileInteractor.loadHeader()
                addLog("[Header] Result: $result")
                result
            }
            val bodyResult = async {
                addLog("[Body] Request started")
                val result = profileInteractor.loadBody()
                addLog("[Body] Result: $result")
                result
            }
            val footerResult = async {
                addLog("[Footer] Request started")
                val result = profileInteractor.loadFooter()
                addLog("[Footer] Result: $result")
                result
            }

            val header = headerResult.await()
            val body = bodyResult.await()
            val footer = footerResult.await()

            addLog("=== Final state ===")
            addLog("Final token: '${TokenStorage.token}'")
            addLog("Final backend valid token: '${mockBackendLogic.validToken}'")

            if (header == null || body == null || footer == null) {
                addLog("❌ ERROR: Race condition occurred! Some requests failed with invalid token")
                addLog("   Header: ${header ?: "FAILED"}")
                addLog("   Body: ${body ?: "FAILED"}")
                addLog("   Footer: ${footer ?: "FAILED"}")
                _state.value = ProfileUiState.Error
            } else {
                addLog("✅ SUCCESS: All requests completed successfully")
                _state.value = ProfileUiState.Success(
                    headerData = header,
                    bodyData = body,
                    footerData = footer
                )
            }
        }
    }

    fun resetToken() {
        TokenStorage.reset()
        mockBackendLogic.validToken = "hashedToken"
        _logs.value = _logs.value + "=== Token reset to 'old_HashedToken' ==="
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun retry() {
        loadProfile()
    }
}
