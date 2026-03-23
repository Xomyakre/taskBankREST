# taskBankREST

Backend на Spring Boot для управления банковскими картами: CRUD карт (ADMIN), просмотр/блокировка своих карт и переводы (USER), JWT (Spring Security) + Liquibase + Swagger/OpenAPI.

## Требования
- Java 17+
- Docker (для PostgreSQL)

## Запуск

1. Поднять БД:
   - `docker compose up -d`

2. Запустить приложение:
   - `./gradlew bootRun`

По умолчанию приложение ожидает PostgreSQL по:
- `DB_URL=jdbc:postgresql://localhost:5432/postgres`
- `DB_USER=postgres`
- `DB_PASSWORD=password`

JWT secret задается через:
- `JWT_SECRET` (по умолчанию: `a9F3kLm2Qw8Zx1R7vT6pN0sD4HjK5uYcE`)

## Dev seed (пользователи для логина)
Liquibase добавляет пользователей:
- `admin / admin123` (роль `ADMIN`)
- `user / user123` (роль `USER`)

## Получение JWT
1. `POST /api/auth/login`
   - body: `{"username":"admin","password":"admin123"}`
2. Подставьте токен в заголовок:
   - `Authorization: Bearer <token>`

## Swagger UI
- Откройте: `http://localhost:8080/swagger-ui.html`

