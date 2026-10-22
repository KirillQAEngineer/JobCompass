# JobCompass

[![JobCompass CI/CD](https://github.com/KirillQAEngineer/JobCompass/actions/workflows/ci-cd.yml/badge.svg?branch=develop)](https://github.com/KirillQAEngineer/JobCompass/actions/workflows/ci-cd.yml)

AI-платформа для автоматизации поиска работы.

## Цель

JobCompass помогает специалистам:

- находить лучшие вакансии;
- оценивать соответствие требованиям;
- автоматически адаптировать резюме;
- генерировать сопроводительные письма;
- готовиться к интервью.

---

## Статус

🚧 Проект находится в активной разработке.

## Публичный контур

- Frontend и документация: GitHub Pages.
- Backend: Render Web Service.
- База данных: Supabase PostgreSQL.

Основные инструкции находятся в `docs/docs/deployment.md`.

Автотесты реализованы единым Java 21/Maven модулем: Selenium WebDriver для UI,
REST Assured для API, JUnit 5, AssertJ и Allure. Учебный CI/CD-контур описан в
`docs/docs/ci-cd.md`.



------------------------------------------------------

Шпаргалка:

make up         # Запустить проект
make down       # Остановить проект
make logs       # Посмотреть логи
make api        # Запустить только API
make web        # Запустить только Frontend
make api-test    # Запустить Java REST Assured тесты
make ui-test     # Запустить Java Selenium тесты
make test        # Запустить оба Java-набора
make lint       # Проверка кода
