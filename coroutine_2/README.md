# Exception Handling in Coroutines

Этот проект содержит три сценария с неправильной обработкой исключений в корутинах. Ваша задача — найти ошибки и исправить их.

## Задача

В проекте реализованы три метода в `UserRepository.kt`, каждый из которых содержит ошибку в обработке исключений:

1. **`getUserDataWithCatch()`** — try/catch вокруг `launch`
2. **`getUserDataWithNestedLaunch()`** — try/catch в родителе для вложенного `launch`
3. **`getUserDataWithAsync()`** — `async` без правильной обработки

Запустите приложение и нажмите на каждую из трёх кнопок. Обратите внимание на поведение ошибок.

---

## Сценарий 1: try/catch вокруг launch

### Проблемный код

```kotlin
suspend fun getUserDataWithCatch(): Triple<User, List<Post>, Settings> {
    var caughtException: Exception? = null

    try {
        scope.launch {
            val user = apiService.fetchUser()
            val posts = apiService.fetchUserPosts(user.id)
            val settings = apiService.fetchUserSettings(user.id)
        }
    } catch (e: Exception) {
        caughtException = e
    }

    if (caughtException != null) {
        throw caughtException
    }
    // ...
}
```

### В чём ошибка?

`try/catch` находится **вокруг** `launch`, но `launch` возвращает управление сразу после запуска корутины. Исключение происходит **внутри** корутины асинхронно, когда `try` блок уже завершён.

### Как исправить?

Переместите `try/catch` **внутрь** лямбда-блока `launch`:

```kotlin
scope.launch {
    try {
        val user = apiService.fetchUser()
        val posts = apiService.fetchUserPosts(user.id)
        val settings = apiService.fetchUserSettings(user.id)
    } catch (e: Exception) {
        // Обработка ошибки
    }
}
```

Или используйте `CoroutineExceptionHandler`:

```kotlin
val handler = CoroutineExceptionHandler { _, exception ->
    // Обработка ошибки
}
scope.launch(handler) {
    val user = apiService.fetchUser()
    // ...
}
```

---

## Сценарий 2: try/catch в родителе для вложенного launch

### Проблемный код

```kotlin
suspend fun getUserDataWithNestedLaunch(): Triple<User, List<Post>, Settings> {
    var caughtException: Exception? = null

    scope.launch {
        try {
            launch {
                val user = apiService.fetchUser()
                // ...
            }
        } catch (e: Exception) {
            caughtException = e
        }
    }.join()
    // ...
}
```

### В чём ошибка?

Вложенный `launch` создаёт **дочернюю** корутину. Исключения в дочерней корутине **не передаются** в `try/catch` родителя. Они передаются вверх по иерархии scope и обрабатываются `CoroutineExceptionHandler` или приводят к отмене родителя.

### Как исправить?

**Вариант 1:** Переместить `try/catch` внутрь вложенного `launch`:

```kotlin
launch {
    launch {
        try {
            val user = apiService.fetchUser()
            // ...
        } catch (e: Exception) {
            // Обработка
        }
    }
}
```

**Вариант 2:** Использовать `supervisorScope` для изоляции ошибок:

```kotlin
supervisorScope {
    val childJob = launch {
        val user = apiService.fetchUser()
        // ...
    }
    childJob.join()
    if (!childJob.isActive) {
        // Обработка ошибки
    }
}
```

---

## Сценарий 3: async без правильной обработки

### Проблемный код

```kotlin
suspend fun getUserDataWithAsync(): Triple<User, List<Post>, Settings> {
    var caughtException: Exception? = null

    try {
        val userDeferred = scope.async {
            apiService.fetchUser()
        }

        val postsDeferred = scope.async {
            val user = userDeferred.await()
            apiService.fetchUserPosts(user.id)
        }

        val settingsDeferred = scope.async {
            val user = userDeferred.await()
            apiService.fetchUserSettings(user.id)
        }

        val user = userDeferred.await()
        val posts = postsDeferred.await()
        val settings = settingsDeferred.await()
        // ...
    } catch (e: Exception) {
        caughtException = e
    }
    // ...
}
```

### В чём ошибка?

Когда `async` запускается в `CoroutineScope` (не в `supervisorScope`), исключение в одном `async` отменяет другие `async`. Кроме того, если `await()` не вызывается для всех deferred, исключение может остаться необработанным.

### Как исправить?

**Вариант 1:** Использовать `supervisorScope` для независимых `async`:

```kotlin
suspend fun getUserData(): Triple<User, List<Post>, Settings> = supervisorScope {
    val userDeferred = async { apiService.fetchUser() }
    val postsDeferred = async { apiService.fetchUserPosts(userDeferred.await().id) }
    val settingsDeferred = async { apiService.fetchUserSettings(userDeferred.await().id) }

    Triple(
        userDeferred.await(),
        postsDeferred.await(),
        settingsDeferred.await()
    )
}
```

**Вариант 2:** Обработать ошибку в каждом `async` отдельно:

```kotlin
val userDeferred = scope.async {
    try {
        apiService.fetchUser()
    } catch (e: Exception) {
        null // или Result wrapper
    }
}
```

**Вариант 3:** Использовать `Result` wrapper:

```kotlin
val userResult = scope.async {
    runCatching { apiService.fetchUser() }
}.await()

userResult.fold(
    onSuccess = { user -> /* ... */ },
    onFailure = { error -> /* ... */ }
)
```

---

## Правильная реализация

Метод `getUserDataCorrect()` показывает правильный подход:

```kotlin
suspend fun getUserDataCorrect(): Triple<User, List<Post>, Settings> = withContext(Dispatchers.IO) {
    val user = apiService.fetchUser()
    val posts = apiService.fetchUserPosts(user.id)
    val settings = apiService.fetchUserSettings(user.id)

    Triple(user, posts, settings)
}
```

Обработка исключений должна происходить **на уровне ViewModel**:

```kotlin
fun loadUserData() {
    viewModelScope.launch {
        _uiState.value = UiState.Loading
        try {
            val result = repository.getUserDataCorrect()
            _uiState.value = UiState.Success(result.first, result.second, result.third)
        } catch (e: Exception) {
            _uiState.value = UiState.Error(e.message ?: "Unknown error")
        }
    }
}
```

---

## Ключевые правила

1. **`try/catch` вокруг `launch` не работает** — перемещайте внутрь
2. **Родитель не ловит исключения дочерних `launch`** — используйте `supervisorScope` или обрабатывайте внутри
3. **`async` без `supervisorScope` отменяет siblings при ошибке** — используйте `supervisorScope` для независимых задач
4. **`await()` должен вызываться для всех `Deferred`** — иначе исключения могут потеряться
5. **Обработка на уровне UI** — бизнес-логика бросает, UI обрабатывает

---

## Тесты

Запустите тесты для проверки поведения:

```bash
./gradlew test
```

Тесты в `ExceptionHandlingTest.kt` проверяют что:
- Исключения действительно происходят
- Текущий код не обрабатывает их корректно
- После исправления тесты должны пройти

---

## Ресурсы

- [Kotlin Coroutines — Exception Handling](https://kotlinlang.org/docs/exception-handling.html)
- [Structured Concurrency](https://kotlinlang.org/docs/structured-concurrency.html)
- [SupervisorScope](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/supervisor-scope.html)
