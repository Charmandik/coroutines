# Lost State: SharedFlow vs StateFlow

## 🐛 Баг

После загрузки профиля поверните экран (Ctrl+F11 в эмуляторе).
**Что должно быть:** профиль отображается сразу, без повторной загрузки.
**Что на самом деле:** экран зависает на Loading и не приходит в себя.

## 🔍 Как воспроизвести

1. `./gradlew installDebug`
2. Открыть приложение
3. Подождать 1.5 сек — появляется профиль Андрея
4. Повернуть экран
5. Наблюдать: Loading… навсегда

## 💡 Почему

Профит в `ProfileViewModel` хранится в `MutableSharedFlow`. SharedFlow с `replay = 0` (значение по умолчанию) не хранит последнее значение. Когда Activity пересоздаётся при повороте, новый подписчик подключается **после** того, как `emit(Success)` уже улетел в пустоту.

```
SharedFlow (replay=0)  ────emit(Success)────▶  ( collector пришёл — пусто )
StateFlow  (replay=1)  ────emit(Success)────▶  ( collector пришёл — Success! )
```

## 🛠 Задача

Переписать `ProfileViewModel` с `SharedFlow` на `StateFlow`, чтобы стейт переживал поворот экрана.

### Требования к решению

- Использовать `MutableStateFlow` или `stateIn()` с `SharingStarted.WhileSubscribed(5000)`
- Код должен компилироваться без ошибок
- При повороте экрана данные не должны запрашиваться повторно

### Файл для изменений

`app/src/main/java/com/rbikbulatov/coroutines_4/presentation/profile/ProfileViewModel.kt`

Остальные файлы (`domain/`, `data/`, `di/`, `ProfileScreen.kt`) **не трогать**.

## ✅ Критерий приёмки

- Собралось (`./gradlew assembleDebug`)
- При повороте экрана профиль отображается без перезагрузки
- В логах нет повторного вызова `fetchProfile()`
