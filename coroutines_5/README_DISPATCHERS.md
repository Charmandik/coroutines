# Dispatcher Bugs - Find & Fix

## Задача

Найти и исправить **3 ошибки блокировки Main thread** в проекте.

---

## Как обнаружить баг

### 1. Запустите приложение и нажмите на любую кнопку

**При нажатии на кнопку:**

- **UI замрёт на 1-2 секунды**
- **Кнопка не будет нажиматься**
- **Анимации остановятся**
- **Может появиться ANR** (Application Not Responding) диалог при длительной блокировке

Это происходит потому, что Main thread заблокирован и не может обрабатывать события UI.

---

### 2. Откройте Logcat в Android Studio

**Фильтр:** `DISPATCHER` или `BUG`

**Что вы увидите:**

```
D/DISPATCHER: === Scenario 1 START ===
D/DISPATCHER: Thread: main
D/DISPATCHER: saveCache running on: main
D/DISPATCHER: Is Main? true
E/BUG: ⚠️ saveCache BLOCKING MAIN THREAD!
D/DISPATCHER: === Scenario 1 END ===
```

**Это проблема!** Операция выполняется на `main` потоке и блокирует его.

---

### 3. После исправления вы увидите

```
D/DISPATCHER: === Scenario 1 START ===
D/DISPATCHER: Thread: main
D/DISPATCHER: saveCache running on: DefaultDispatcher-worker-1
D/DISPATCHER: Is Main? false
I/BUG: ✅ saveCache running on background thread
D/DISPATCHER: === Scenario 1 END ===
```

**Отлично!** Операция выполняется на фоновом потоке, UI не блокируется.

---

## Что нужно сделать

1. Запустить приложение и нажать на каждую кнопку
2. Открыть Logcat и увидеть сообщения о блокировке
3. Найти в коде `DataRepository.kt` все 3 сценария
4. Для каждой блокировки:
   - Определить тип операции (IO, CPU, Network)
   - Выбрать правильный `Dispatcher`
   - Обернуть блокирующий код в `withContext(Dispatchers.X)`
5. Проверить, что приложение больше не зависает и в Logcat нет ошибок

---

## Подсказки

### Dispatchers Cheat Sheet

| Dispatcher | Для чего | Примеры |
|------------|----------|---------|
| `Dispatchers.Main` | UI операции | Обновление состояния, TextView |
| `Dispatchers.IO` | I/O операции | Сеть, БД, файлы |
| `Dispatchers.Default` | CPU-heavy вычисления | Сложный парсинг, сортировка, хеширование |
| `Dispatchers.Unconfined` | **Не использовать!** | Только для тестов |

### Красные флаги 🔴

- `Thread.sleep()` в корутине
- Чтение/запись файлов без `withContext(Dispatchers.IO)`
- Сетевые вызовы без `withContext(Dispatchers.IO)`
- Сложные вычисления в UI-корутине

### Правило

> Запускай в `viewModelScope` (Main), но переключайся на правильный 
> Dispatcher для каждой операции через `withContext`

---

## Сценарии

### Scenario 1: File I/O

**Где искать:** `DataRepository.kt` → `loadUserProfileScenario1()`

**Проблема:** Операции с файлами блокируют Main thread

```kotlin
// ❌ ПЛОХО
scope.launch {
    fileManager.saveCache("user_${user.id}") // БЛОКИРОВКА!
}

// ✅ ХОРОШО
scope.launch {
    withContext(Dispatchers.IO) {
        fileManager.saveCache("user_${user.id}")
    }
}
```

---

### Scenario 2: Network + Cache

**Где искать:** `DataRepository.kt` → `loadUserProfileScenario2()`

**Проблема:** Сетевые вызовы и кэширование в Main thread

```kotlin
// ❌ ПЛОХО
scope.launch {
    val user = apiService.fetchUser() // БЛОКИРОВКА!
    val cachedData = fileManager.loadCache() // БЛОКИРОВКА!
}

// ✅ ХОРОШО
scope.launch {
    val user = withContext(Dispatchers.IO) {
        apiService.fetchUser()
    }
    val cachedData = withContext(Dispatchers.IO) {
        fileManager.loadCache()
    }
}
```

---

### Scenario 3: CPU-heavy

**Где искать:** `DataRepository.kt` → `loadUserProfileScenario3()`

**Проблема:** Тяжелые вычисления блокируют UI

```kotlin
// ❌ ПЛОХО
scope.launch {
    fileManager.deleteOldFiles() // БЛОКИРОВКА!
}

// ✅ ХОРОШО
scope.launch {
    withContext(Dispatchers.IO) {
        fileManager.deleteOldFiles()
    }
}
```

---

## Правильная реализация

Метод `loadUserProfileCorrect()` показывает, как должно быть:

```kotlin
suspend fun loadUserProfileCorrect(): UserProfile = withContext(Dispatchers.IO) {
    val userResponse = apiService.fetchUser()
    val postsResponse = apiService.fetchUserPosts(userResponse.id)
    
    fileManager.saveCache("user_${userResponse.id}")
    
    // ... маппинг и возврат результата
}
```

Обратите внимание:
- Весь IO-код обернут в `withContext(Dispatchers.IO)`
- UI обновляется автоматически после возврата в `viewModelScope`

---

## Проверка

После исправления:
1. Приложение **не должно зависать** при нажатии на кнопки
2. В Logcat **нет сообщений** `⚠️ BLOCKING MAIN THREAD!`
3. В Logcat **есть сообщения** `✅ running on background thread`
4. Данные загружаются корректно
5. Метод `loadUserProfileCorrect()` работает правильно — используйте его как референс

---

## Дополнительные инструменты отладки

### Android Studio Profiler

1. View → Tool Windows → Profiler
2. Выбрать запущенное приложение
3. Нажать на кнопку в приложении
4. На графике **CPU** будет видно загрузку Main thread

### StrictMode (для продвинутых)

Добавьте в `MainActivity.onCreate()`:

```kotlin
StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
    .detectAll()
    .penaltyLog()
    .penaltyFlashScreen()
    .build())
```

Экран будет мигать зелёным при блокировке Main thread.

---
