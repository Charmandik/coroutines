# Инструкция: Демонстрация Race Condition при обновлении токена

## 🎯 Цель
Показать проблему гонки потоков (race condition), когда три параллельных запроса одновременно пытаются обновить устаревший токен авторизации.

---

## 📋 Как воспроизвести ошибку

### Шаг 1: Запустите приложение
```bash
# Откройте проект в Android Studio и запустите на эмуляторе/устройстве
# Или через командную строку:
./gradlew :app:installDebug
```

### Шаг 2: Нажмите кнопку "Load Profile"
- Три запроса (`loadHeader`, `loadBody`, `loadFooter`) запускаются **параллельно** через `async`
- Все три запроса начинают с **устаревшим токеном** `"old_HashedToken"`

### Шаг 3: Наблюдайте за логами внизу экрана
Вы увидите следующую последовательность:

```
=== Starting 3 parallel requests ===
Initial token: 'old_HashedToken'
Backend valid token: 'old_HashedToken'
[Header] Request started
[Body] Request started
[Footer] Request started
[API] loadHeader with token='old_HashedToken'
[API] loadBody with token='old_HashedToken'
[API] loadFooter with token='old_HashedToken'

[Backend] loadHeader called with token: 'old_HashedToken', valid token: 'old_HashedToken'
[Backend] loadBody called with token: 'old_HashedToken', valid token: 'old_HashedToken'
[Backend] loadFooter called with token: 'old_HashedToken', valid token: 'old_HashedToken'
[Backend] Token validation: 'old_HashedToken' == 'old_HashedToken' → false
[Backend] Token validation: 'old_HashedToken' == 'old_HashedToken' → false
[Backend] Token validation: 'old_HashedToken' == 'old_HashedToken' → false

[API] loadHeader got 403, refreshing token...
[API] loadBody got 403, refreshing token...
[API] loadFooter got 403, refreshing token...

[Backend] refreshToken called with old token: 'old_HashedToken'
[Backend] Token updated to: 'old_HashedToken_new'
[Backend] refreshToken called with old token: 'old_HashedToken'
[Backend] Token updated to: 'old_HashedToken_new_new'
[Backend] refreshToken called with old token: 'old_HashedToken'
[Backend] Token updated to: 'old_HashedToken_new_new_new'

[Header] Result: null
[Body] Result: null
[Footer] Result: null

=== Final state ===
Final token: 'old_HashedToken'
Final backend valid token: 'old_HashedToken_new_new_new'
❌ ERROR: Race condition occurred! Some requests failed with invalid token
   Header: FAILED
   Body: FAILED
   Footer: FAILED
```

### Шаг 4: Наблюдайте за UI
- После ошибки на экране отображается **"Error loading profile"**
- Логи показывают, что токен был обновлён **3 раза**: `old_HashedToken` → `old_HashedToken_new` → `old_HashedToken_new_new` → `old_HashedToken_new_new_new`

### Шаг 5: Повторите эксперимент
- Нажмите **"Reset Token"** для сброса токена к начальному значению
- Нажмите **"Retry"** или **"Load Profile"** чтобы повторить

---

## 🔍 Что происходит (объяснение проблемы)

```
Время 0ms:   Все 3 запроса начинают с токеном "old_HashedToken"
             TokenStorage.token = "old_HashedToken"
             Backend.validToken = "old_HashedToken"

Время 100ms: Все 3 запроса получают "403 error" (токен устарел)

Время 100ms: [Header] вызывает refreshToken()
             Backend.validToken = "old_HashedToken_new"
             
Время 100ms: [Body] вызывает refreshToken() (одновременно!)
             Backend.validToken = "old_HashedToken_new_new"
             
Время 100ms: [Footer] вызывает refreshToken() (одновременно!)
             Backend.validToken = "old_HashedToken_new_new_new"

Время 150ms: [Header] повторяет запрос с токеном "old_HashedToken_new"
             Но backend ожидает "old_HashedToken_new_new_new" → 403 → null
             
Время 150ms: [Body] повторяет запрос с токеном "old_HashedToken_new_new"
             Но backend ожидает "old_HashedToken_new_new_new" → 403 → null
             
Время 150ms: [Footer] повторяет запрос с токеном "old_HashedToken_new_new_new"
             УСПЕХ! (последний запрос с правильным токеном)
```

**Итог:** Из-за гонки потоков токен обновился 3 раза вместо 1, и только последний запрос ушёл с валидным токеном.

---

## 📊 Как это видно в логах Android

Откройте **Logcat** в Android Studio и фильтруйте по тегам:

1. **`myAppLogs`** - логи приложения (запросы, ответы, состояние)
2. **`myAppLogsBackend`** - логи бэкенда (валидация токена, обновление)

---

## ⚠️ Почему это проблема в реальных проектах

1. **Сервер может заблокировать токен** после нескольких обновлений
2. **Пользователь получит ошибки** вместо данных
3. **Лишняя нагрузка на сервер** (3 запроса на обновление вместо 1)
4. **Непредсказуемое поведение** (иногда работает, иногда нет)

---

## 🔧 Решение: Double-Checked Locking

### Проблема
Несколько корутин одновременно видят 403 и каждая вызывает `refreshToken()`.

### Решение: Double-Checked Locking с `Mutex`

```kotlin
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ProfileApi(
    private val mockBackendLogic: MockBackendLogic = MockBackendLogic()
) {
    private val tokenRefreshMutex = Mutex()

    // ✅ Универсальная функция для любого запроса
    private suspend fun <T> executeWithTokenRefresh(
        apiCall: suspend (String) -> T
    ): T? {
        val token = TokenStorage.token
        val response = apiCall(token)

        if (response == "403 error") {
            // Первая проверка - без блокировки
            if (TokenStorage.token == token) {
                // Вторая проверка - с блокировкой (Double Check)
                tokenRefreshMutex.withLock {
                    if (TokenStorage.token == token) {
                        // Токен всё ещё старый - обновляем
                        refreshToken(token)
                    }
                    // Иначе - другая корутина уже обновила, используем новый
                }
            }
            // Повторяем запрос с новым токеном
            return apiCall(TokenStorage.token)
        }
        return response
    }

    suspend fun loadHeader(): String? = executeWithTokenRefresh { token ->
        mockBackendLogic.loadHeader(token)
    }

    suspend fun loadBody(): String? = executeWithTokenRefresh { token ->
        mockBackendLogic.loadBody(token)
    }

    suspend fun loadFooter(): String? = executeWithTokenRefresh { token ->
        mockBackendLogic.loadFooter(token)
    }

    suspend fun refreshToken(token: String): String {
        return mockBackendLogic.refreshToken(token)
    }
}
```

### Как это работает:

```
Время 100ms: [Header] получает 403
             → Проверка 1: TokenStorage.token == old? → ДА
             → Захватывает Mutex
             → Проверка 2: TokenStorage.token == old? → ДА
             → Обновляет токен: old → new
             → Освобождает Mutex

Время 100ms: [Body] получает 403
             → Проверка 1: TokenStorage.token == old? → НЕТ (уже new!)
             → НЕ захватывает Mutex (сразу идёт к повторному запросу)
             → Повторяет запрос с new → ✅

Время 100ms: [Footer] получает 403
             → Проверка 1: TokenStorage.token == old? → НЕТ (уже new!)
             → НЕ захватывает Mutex
             → Повторяет запрос с new → ✅
```

### Преимущества Double-Checked Locking:

| Обычный Mutex | Double-Checked Locking |
|--------------|----------------------|
| Все 3 корутины ждут в `withLock` | Только 1-я захватывает lock |
| 2-я и 3-я ждут хотя уже не нужно | 2-я и 3-я видят новый токен и сразу идут дальше |
| Больше накладных расходов | Меньше блокировок = лучше производительность |

### Ключевые моменты:

1. **Первая проверка (без lock)** - быстрая проверка, не нужно ли вообще обновлять токен
2. **Вторая проверка (с lock)** - гарантируем что только одна корутина обновит токен
3. **После lock** - используем актуальный токен из `TokenStorage.token`

### Преимущества:

- ✅ Логика с Mutex написана **один раз**
- ✅ Легко добавлять новые методы
- ✅ Нет дублирования кода
- ✅ **Лучшая производительность** - меньше блокировок
- ✅ Все запросы уходят с валидным токеном
