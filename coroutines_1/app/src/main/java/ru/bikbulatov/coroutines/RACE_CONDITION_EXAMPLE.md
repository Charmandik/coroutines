# Пример неправильного поведения: Race Condition при поиске

## Описание проблемы

Этот проект демонстрирует классическую проблему **race condition** (состояние гонки) при реализации поиска в Android-приложении.

## Сценарий проблемы

1. Пользователь быстро вводит текст "and" в поле поиска
2. При вводе каждой буквы запускается новый запрос на поиск:
   - Ввод "a" → запрос #1 (задержка 800мс)
   - Ввод "an" → запрос #2 (задержка 600мс)
   - Ввод "and" → запрос #3 (задержка 400мс)

3. Из-за разной задержки запросы завершаются в неправильном порядке:
   - Запрос #3 ("and") завершается первым → показывает правильные результаты
   - Запрос #2 ("an") завершается вторым → **перезаписывает результаты устаревшими данными**
   - Запрос #1 ("a") завершается последним → **показывает совершенно не те результаты**

## Причина проблемы

В [`VacancyListViewModel.kt`](presentation/VacancyListViewModel.kt:67) используется неправильный подход:

```kotlin
// НЕПРАВИЛЬНО: не отменяем предыдущие запросы
searchJob = viewModelScope.launch {
    val result = vacancyRepository.searchVacancies(query)
    result.onSuccess { vacancies ->
        _vacancies.value = vacancies  // Проблема: всегда обновляем!
    }
}
// Обратите внимание: searchJob?.cancel() НЕ вызывается!
```

## Как воспроизвести

1. Запустите приложение
2. Быстро введите текст "and" в поле поиска (буква за буквой без пауз)
3. Наблюдайте за логами в Logcat:
   ```
   🔍 Начало поиска: 'a' в 1234567890
   🔍 Начало поиска: 'an' в 1234567900
   🔍 Начало поиска: 'and' в 1234567910
   ✅ Поиск завершён: 'and' найдено 2 вакансий в 1234568310
   ✅ Поиск завершён: 'an' найдено 3 вакансий в 1234568510  ← ПЕРЕЗАПИСАЛ!
   ✅ Поиск завершён: 'a' найдено 5 вакансий в 1234568710   ← ФИНАЛЬНЫЙ РЕЗУЛЬТАТ (неправильный!)
   ```

## Правильное решение

### Вариант 1: Отмена предыдущего запроса

```kotlin
private var searchJob: Job? = null

fun onSearchQueryChange(query: String) {
    _searchQuery.value = query
    
    // ПРАВИЛЬНО: отменяем предыдущий запрос
    searchJob?.cancel()
    
    searchJob = viewModelScope.launch {
        val result = vacancyRepository.searchVacancies(query)
        result.onSuccess { vacancies ->
            _vacancies.value = vacancies
        }
    }
}
```

### Вариант 2: Использование debounce

```kotlin
// В ViewModel
private val searchQuery = MutableStateFlow("")
private val searchResults = MutableStateFlow<List<Vacancy>>(emptyList())

init {
    searchQuery
        .debounce(300)  // Ждём 300мс после последнего ввода
        .distinctUntilChanged()
        .flatMapLatest { query ->
            flow {
                emit(vacancyRepository.searchVacancies(query).getOrNull() ?: emptyList())
            }
        }
        .onEach { results ->
            searchResults.value = results
        }
        .launchIn(viewModelScope)
}

fun onSearchQueryChange(query: String) {
    searchQuery.value = query
}
```

### Вариант 3: Проверка актуальности запроса

```kotlin
searchJob = viewModelScope.launch {
    val currentQuery = query
    val result = vacancyRepository.searchVacancies(query)
    
    // Проверяем, не изменился ли запрос за время выполнения
    if (_searchQuery.value == currentQuery) {
        result.onSuccess { vacancies ->
            _vacancies.value = vacancies
        }
    }
    // Если запрос изменился, просто игнорируем результат
}
```

## Ключевые выводы

1. **Всегда отменяйте предыдущие корутины** при запуске новой, если они конкурируют за одни и те же данные
2. **Используйте `debounce`** для обработки пользовательского ввода
3. **Проверяйте актуальность данных** перед обновлением UI
4. **Используйте `flatMapLatest`** вместо `flatMapConcat` или `flatMapMerge` для поисковых запросов
