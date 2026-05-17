package ru.bikbulatov.coroutines.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.bikbulatov.coroutines.domain.LoadVacanciesUseCase
import ru.bikbulatov.coroutines.domain.Vacancy
import ru.bikbulatov.coroutines.domain.VacancyRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class VacancyListViewModel @Inject constructor(
    private val loadVacanciesUseCase: LoadVacanciesUseCase,
    private val vacancyRepository: VacancyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VacancyListUiState>(VacancyListUiState.Loading)
    val uiState: StateFlow<VacancyListUiState> = _uiState.asStateFlow()

    private val _allVacancies = MutableStateFlow<List<Vacancy>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _vacancies = MutableStateFlow<List<Vacancy>>(emptyList())
    private val _isSearching = MutableStateFlow(false)
    private val _lastProcessedQuery = MutableStateFlow("")

    val vacancies: StateFlow<List<Vacancy>> = _vacancies.asStateFlow()
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    val lastProcessedQuery: StateFlow<String> = _lastProcessedQuery.asStateFlow()

    private var searchJob: Job? = null

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

        searchJob = viewModelScope.launch {
            _isSearching.value = true

            // Логгируем начало поиска для отладки
            println("🔍 Начало поиска: '$query' в ${System.currentTimeMillis()}")

            val result = vacancyRepository.searchVacancies(query)

            result.onSuccess { vacancies ->
                // Небольшая дополнительная задержка перед обновлением UI
                // чтобы усилить эффект "неправильного" поведения
                delay(50)

                // НЕПРАВИЛЬНО: всегда обновляем результаты, даже если
                // запрос устарел (пользователь уже ввёл другой запрос)
                _vacancies.value = vacancies
                _lastProcessedQuery.value = query
                _isSearching.value = false

                println(
                    "✅ Поиск завершён: '$query' найдено ${vacancies.size} вакансий в ${
                        LocalDateTime.now().format(
                            DateTimeFormatter.ISO_DATE_TIME
                        )
                    }"
                )
            }.onFailure { error ->
                _isSearching.value = false
                println("❌ Ошибка поиска: '${query}' - ${error.message}")
            }
        }
        // Обратите внимание: мы НЕ вызываем searchJob?.cancel() перед новым запуском!
        // Это и есть основная ошибка - предыдущие запросы продолжают выполняться
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
