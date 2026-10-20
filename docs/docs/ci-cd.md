---
id: ci-cd
title: CI/CD и практика автотестов
---

# CI/CD и практика автотестов

В JobCompass настроен настоящий GitHub Actions pipeline. Он работает на бесплатных GitHub-hosted runners и запускается автоматически после создания или обновления Pull Request в `develop` или `main`.

Workflow находится в `.github/workflows/ci-cd.yml`.

## Схема работы

1. Разработчик создаёт отдельную ветку от `develop`.
2. Добавляет код и автотесты, делает commit и push.
3. Создаёт Pull Request в `develop`.
4. GitHub запускает Java API, frontend build, Java UI и documentation jobs.
5. После их успешного завершения собирается Docker-образ backend.
6. Общий check `CI Passed` становится зелёным только после прохождения всех обязательных этапов.
7. Изменения объединяются с `develop`.
8. Когда готовый `develop` объединяется с `main`, pipeline запускается повторно и публикует проверенный Flutter Web вместе с документацией в GitHub Pages.

Публикация при Pull Request не выполняется. PR только проверяет код и создаёт временные build-артефакты.

## Этапы pipeline

| Job | Что проверяется | Результат |
| --- | --- | --- |
| Java API | PostgreSQL 17, Alembic, FastAPI, REST Assured | Surefire, Allure и API log |
| Frontend | Dart format, Flutter Analyze, Web build | Собранный сайт |
| Java UI | PostgreSQL 17, FastAPI, Flutter Web, Selenium | Surefire, Allure, screenshots и logs |
| Documentation | Чистая установка npm, audit moderate, Docusaurus build | Собранная документация |
| Package | Сборка backend Docker image | Проверенный Docker-образ внутри runner |
| CI Passed | Результаты всех обязательных jobs | Общий зелёный или красный check |
| CD | Сборка Pages artifact и deploy | Выполняется только для успешного `main` |

Каждый job запускается в новом временном окружении. Backend получает отдельный PostgreSQL 17 service container, поэтому тесты не используют локальную или production-базу.

## Первый учебный Pull Request

Создайте ветку от актуального `develop`:

```bash
git switch develop
git pull
git switch -c test/my-first-autotest
```

Добавьте или измените тест, затем проверьте его локально:

```bash
make lint
make test
```

Отправьте ветку:

```bash
git add .
git commit -m "test: add my first autotest"
git push -u origin test/my-first-autotest
```

На GitHub создайте Pull Request: `test/my-first-autotest` → `develop`. Во вкладке **Checks** появятся отдельные jobs. Нажатие на job открывает последовательность шагов и полный log.

## Где писать Java-автотесты

Единый Maven-модуль находится в `qa-tests`:

- `api` — REST Assured проверки HTTP-контрактов и бизнес-процессов;
- `ui` — Selenium пользовательские сценарии;
- `ui/pages` — Page Object;
- `support` — подготовка тестовых пользователей в изолированной CI-базе;
- `config` — параметры окружения.

Минимальный API-тест:

```java
@Test
@Tag("api")
void healthEndpointIsAvailable() {
    int status = RestAssured.given()
            .baseUri(TestConfig.apiBaseUrl())
            .get("/")
            .statusCode();

    assertThat(status).isEqualTo(200);
}
```

Запустить API-класс:

```bash
mvn -f qa-tests/pom.xml -Papi-tests -Dtest=AuthSecurityApiTest test
```

Запустить UI-класс:

```bash
mvn -f qa-tests/pom.xml -Pui-tests -Dtest=AuthUiTest test
```

Тест не должен обращаться к production Supabase или зависеть от реального
внешнего job provider. UI-локаторы выносятся в Page Object, а API-подготовка
данных выполняется только через support-классы.

## Как потренировать падение pipeline

1. Создайте учебную ветку.
2. Добавьте Java-тест с заведомо неправильным AssertJ-ожиданием.
3. Сделайте push и откройте PR в `develop`.
4. Откройте красный backend job и найдите в log имя упавшего теста, expected и actual result.
5. Исправьте ожидание на правильное.
6. Сделайте новый commit и push в ту же ветку.
7. GitHub отменит устаревший run, запустит новый и обновит check в существующем PR.

Не объединяйте намеренно сломанный тест с `develop`.

## Артефакты

Внизу страницы завершённого workflow можно скачать:

- `java-api-test-reports-*` — Surefire, Allure и log тестового API;
- `java-ui-test-reports-*` — Surefire, Allure, screenshots и server logs;
- `web-app-*` — production build Flutter Web;
- `documentation-*` — production build Docusaurus.

Учебные артефакты хранятся 7 дней.

## Локальный эквивалент CI

Основные команды:

```bash
make lint
make api-test
make ui-test
make docs-build
make web-build
make docker-build
```

Полная локальная последовательность:

```bash
make ci
```

Для Java-тестов требуются JDK 21 и Maven 3.9+. Полная подготовка локальных
API, PostgreSQL и Flutter Web описана на странице [Testing](./testing.md).

## Branch protection

Чтобы нельзя было случайно объединить красный PR:

1. Откройте GitHub repository → **Settings** → **Branches** или **Rules**.
2. Создайте правило для `develop`, затем для `main`.
3. Включите **Require a pull request before merging**.
4. Включите **Require status checks to pass before merging**.
5. Выберите обязательный check `CI Passed`.
6. Для `main` дополнительно запретите прямой push.

Сначала дождитесь хотя бы одного завершённого pipeline: GitHub показывает check в списке только после его первого запуска.

## Что делать при ошибке

- `Java API` — найти первый REST Assured test; проверить API log и миграции.
- `Java UI` — открыть screenshot, page source, UI/API logs и первый failed test.
- `Frontend` — проверить format, analyzer и Web build.
- `Documentation` — проверить npm install, broken link или ошибку MDX.
- `Package` — проверить Dockerfile и наличие файлов в build context.
- `CI Passed` — посмотреть, какой предыдущий job имеет `failure`, `cancelled` или `skipped`.

Кнопка **Re-run failed jobs** полезна только для временного сбоя runner. Ошибку в коде нужно исправлять новым commit.
