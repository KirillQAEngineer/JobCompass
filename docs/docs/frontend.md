---
id: frontend
title: Frontend
---

# Frontend

Frontend JobCompass реализован на Flutter Web.

## Где находится frontend

```text
jobcompass_ui
```

Основные директории:

- `lib/features` - экраны и UI по функциональным разделам.
- `lib/providers` - Riverpod providers.
- `lib/models` - модели данных.
- `lib/core/network` - API client.
- `lib/core/localization` - локализация.
- UI проверяется внешним Java-модулем `qa-tests` через Selenium WebDriver.

## Локальный запуск

```bash
cd jobcompass_ui
flutter pub get
flutter run -d chrome --web-port 5124
```

## Настройка backend URL

По умолчанию frontend использует:

```text
http://localhost:8000
```

Для сборки с другим backend:

```bash
flutter build web \
  --release \
  --dart-define=API_BASE_URL=https://api.example.com
```

## Сборка для GitHub Pages

GitHub Pages для project site обычно требует base href вида `/<repo>/`.

Пример:

```bash
flutter build web \
  --release \
  --base-href /JobCompass/ \
  --dart-define=API_BASE_URL=http://localhost:8000
```

В проекте это автоматизировано через GitHub Actions workflow.

## Автоматические UI-тесты

Selenium-тесты и Page Object находятся в:

```text
qa-tests/src/test/java/com/jobcompass/qa/ui
```

После запуска backend, тестовой БД и Flutter Web:

```bash
mvn -f qa-tests/pom.xml -Pui-tests test
```

Статическая проверка самого Flutter-кода остаётся частью frontend build:

```bash
cd jobcompass_ui
dart format --output=none --set-exit-if-changed lib
flutter analyze --fatal-infos --fatal-warnings
```

## Локализация

Тексты находятся в:

```text
jobcompass_ui/lib/core/localization/app_localizations.dart
```

При добавлении нового текста нужно добавить ключ в русскую и английскую секции.
