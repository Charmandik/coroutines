package ru.bikbulatov.coroutines.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.bikbulatov.coroutines.domain.LoadVacanciesUseCase
import ru.bikbulatov.coroutines.domain.Vacancy
import javax.inject.Inject

@HiltViewModel
class VacancyListViewModel @Inject constructor(
    private val loadVacanciesUseCase: LoadVacanciesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<VacancyListUiState>(VacancyListUiState.Loading)
    val uiState: StateFlow<VacancyListUiState> = _uiState.asStateFlow()

    private val _allVacancies = MutableStateFlow<List<Vacancy>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _vacancies = MutableStateFlow<List<Vacancy>>(emptyList())

    val vacancies: StateFlow<List<Vacancy>> = _vacancies.asStateFlow()

    init {
        loadVacancies()
    }

    fun loadVacancies() {
        viewModelScope.launch {
            _uiState.value = VacancyListUiState.Loading
            loadVacanciesUseCase()
                .onSuccess { vacancies ->
                    _allVacancies.value = vacancies
                    applyFilter()
                    _uiState.value = VacancyListUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = VacancyListUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        applyFilter()
    }

    private fun applyFilter() {
        val query = _searchQuery.value.trim().lowercase()
        _vacancies.value = if (query.isEmpty()) {
            _allVacancies.value
        } else {
            _allVacancies.value.filter { vacancy ->
                vacancy.title.lowercase().contains(query) ||
                vacancy.company.lowercase().contains(query) ||
                vacancy.location?.lowercase()?.contains(query) == true ||
                vacancy.description?.lowercase()?.contains(query) == true
            }
        }
    }

    fun onVacancyClick(vacancy: Vacancy) {
        // Обработка клика по вакансии
        // Можно добавить навигацию или другое действие
    }
}

sealed class VacancyListUiState {
    data object Loading : VacancyListUiState()
    data object Success : VacancyListUiState()
    data class Error(val message: String) : VacancyListUiState()
}
