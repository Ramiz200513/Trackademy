# Trackademy

Мобильное приложение EdTech-платформы для учебных центров: расписание, посещаемость,
домашние задания, учебные материалы и успеваемость. Android и iOS из одной кодовой базы
на **Kotlin Multiplatform + Compose Multiplatform**.

[![CI](https://github.com/Ramiz200513/Trackademy/actions/workflows/ci.yml/badge.svg)](https://github.com/Ramiz200513/Trackademy/actions/workflows/ci.yml)

## Возможности

| Роль | Функции |
|---|---|
| Все | Вход с выбором учебного центра (multi-tenant), JWT-сессия с автообновлением токена |
| Студент | Успеваемость (средний балл, посещаемость, оценки), расписание, задания со сдачей работ (фото/файлы), учебные материалы с поиском, просмотром и скачиванием |
| Преподаватель | Дашборд, отметка посещаемости, создание и проверка заданий, выставление оценок |
| Администратор | Регистрация учебного центра по ключу активации |
| Геймификация | Внутренние монеты и магазин наград |

## Стек

- **UI:** Compose Multiplatform, Material 3, Coil 3
- **Архитектура:** Clean Architecture (domain / data / presentation), MVI, Decompose (навигация и жизненный цикл)
- **DI:** Koin
- **Сеть:** Ktor Client, kotlinx.serialization, bearer-авторизация с refresh token, таймауты
- **Асинхронность:** Kotlin Coroutines, Flow / StateFlow
- **Хранение:** multiplatform-settings
- **Тесты:** kotlin-test, kotlinx-coroutines-test, Ktor MockEngine
- **CI/CD:** GitHub Actions (тесты, lint, сборки, подписанные релизы по тегу)

## Архитектура

```
composeApp/src/
├── commonMain/                 общий код Android + iOS
│   ├── core/
│   │   ├── config/             AppConfig (expect/actual): URL API, debug-флаг
│   │   ├── coroutines/         DispatcherProvider
│   │   ├── di/                 модули Koin
│   │   ├── domain/             NetworkError
│   │   ├── logging/            AppLogger (только debug-сборки)
│   │   ├── network/            HttpClientFactory, safeApiCall, Result
│   │   ├── presentation/       BaseComponent (MVI: state + effects + lifecycle scope)
│   │   ├── session/            SessionStorage, UserRole
│   │   └── ui/                 тема, общие компоненты, тексты ошибок
│   ├── domain/                 модели и интерфейсы репозиториев
│   ├── data/                   DTO, мапперы, реализации репозиториев
│   ├── feature/                экраны: auth, schedule, assignments, materials, performance, teacher, profile
│   └── root/                   корневая навигация (Decompose)
├── androidMain/                Android: Application, FileSaver, AppConfig
├── iosMain/                    iOS: точка входа, FileSaver, AppConfig
└── commonTest/                 unit-тесты общего кода
```

Поток данных: `Screen (Compose)` → `Intent` → `Component` → `Repository` → `Ktor` → `Result<T, NetworkError>` → `StateFlow<State>` → `Screen`.

## Сборка

Требования: JDK 17, Android SDK, для iOS — macOS с Xcode.

```bash
# Android debug
./gradlew :composeApp:assembleDebug

# Unit-тесты
./gradlew :composeApp:testDebugUnitTest

# Release с другим API
./gradlew :composeApp:assembleRelease -PapiBaseUrl=https://api.example.com/
```

iOS: открыть `iosApp/iosApp.xcodeproj` в Xcode и запустить.

### Подпись релиза

Локально — файл `keystore.properties` в корне (в git не попадает):

```properties
storeFile=release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

В CI — секреты репозитория: `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`,
`ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`. Релиз собирается при пуше тега `v*`:

```bash
git tag v1.0.0 && git push origin v1.0.0
```
