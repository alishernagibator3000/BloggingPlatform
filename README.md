# Blogging Platform — Backend API

> Полноценный REST API бэкенд для платформы микроблогинга, аналогичный Twitter/Threads.  
> Реализует аутентификацию через JWT, ролевую модель доступа, ленты публикаций, лайки, подписки и комментарии.

---

## 📋 Содержание

- [Описание проекта](#описание-проекта)
- [Технологии](#технологии)
- [Архитектура](#архитектура)
- [Сущности базы данных](#сущности-базы-данных)
- [Роли и права доступа](#роли-и-права-доступа)
- [API Endpoints](#api-endpoints)
- [Запуск проекта](#запуск-проекта)
- [Тестирование](#тестирование)
- [Обработка ошибок](#обработка-ошибок)

---

## Описание проекта

**Microblog Platform** — это серверная часть социальной сети для коротких публикаций. Проект реализован на **Java 17 + Spring Boot 3** и предоставляет REST API для:

- регистрации и входа пользователей (JWT-авторизация);
- создания, просмотра и удаления постов;
- подписки на других пользователей и личной ленты;
- комментирования постов;
- лайков;
- администрирования пользователей (роли, блокировка).

Проект задокументирован через **Swagger UI** и покрыт юнит-тестами на ключевых сервисах.

---

## Технологии

| Технология | Версия | Назначение |
|---|---|---|
| Java | 17 | Язык программирования |
| Spring Boot | 3.3.5 | Основной фреймворк |
| Spring Security + JWT (jjwt) | 0.12.6 | Аутентификация и авторизация |
| Spring Data JPA | — | ORM и работа с БД |
| PostgreSQL | — | Реляционная СУБД |
| Hibernate | — | JPA-провайдер |
| Lombok | — | Снижение boilerplate-кода |
| Springdoc OpenAPI | 2.6.0 | Swagger UI документация |
| JUnit 5 + Mockito | — | Юнит-тестирование |
| Maven | — | Сборка проекта |

---

## Архитектура

Проект следует классической **многослойной архитектуре** (Layered Architecture):

```
com.microblog
├── config/           # Конфигурация: Spring Security, JWT-фильтр, OpenAPI
├── controllers/      # REST-контроллеры (входная точка HTTP-запросов)
├── dto/
│   ├── request/      # Входные DTO (валидация через jakarta.validation)
│   └── response/     # Выходные DTO (данные, возвращаемые клиенту)
├── entities/         # JPA-сущности (User, Post, Comment, Like, Subscription)
├── exceptions/       # Кастомные исключения + GlobalExceptionHandler
├── repositories/     # Spring Data JPA репозитории
└── services/
    ├── *.java        # Интерфейсы сервисов
    └── impl/         # Реализации бизнес-логики
```

**Поток запроса:**
```
HTTP Request
    → JwtAuthenticationFilter          (проверка токена)
    → Controller                       (маршрутизация, входные DTO)
    → Service (impl)                   (бизнес-логика)
    → Repository                       (доступ к БД через JPA)
    → Response DTO → HTTP Response
```

---

## Сущности базы данных

| Сущность | Поля | Описание |
|---|---|---|
| **User** | id, username, email, password, bio, role, blocked, createdAt | Пользователи системы |
| **Post** | id, content, user, createdAt | Публикации пользователей |
| **Comment** | id, text, post, user, createdAt | Комментарии к постам |
| **Subscription** | id, follower, followed, createdAt | Подписки (follower → followed) |
| **Like** | id, user, post, createdAt | Лайки (user ↔ post, уникальная пара) |

**Связи:**
- `User` 1 → N `Post`, `Comment`, `Like`
- `Post` 1 → N `Comment`, `Like`
- `User` M ↔ N `User` (через `Subscription`)

---

## Роли и права доступа

| Действие | USER | MANAGER | ADMIN |
|---|:---:|:---:|:---:|
| Регистрация / вход | ✅ | ✅ | ✅ |
| Читать глобальную ленту | ✅ | ✅ | ✅ |
| Читать личную ленту | ✅ | ✅ | ✅ |
| Создавать посты и комментарии | ✅ | ✅ | ✅ |
| Лайкать посты | ✅ | ✅ | ✅ |
| Подписываться / отписываться | ✅ | ✅ | ✅ |
| Удалять **свои** посты / комментарии | ✅ | ✅ | ✅ |
| Удалять **любые** посты / комментарии | ❌ | ✅ | ✅ |
| Список всех пользователей | ❌ | ❌ | ✅ |
| Изменять роли пользователей | ❌ | ❌ | ✅ |
| Блокировать / разблокировать пользователей | ❌ | ❌ | ✅ |

Заблокированный пользователь не может войти в систему (Spring Security `isAccountNonLocked`).

---

## API Endpoints

### 🔐 Auth — `/api/auth`

| Метод | URL | Тело запроса | Описание | Доступ |
|---|---|---|---|---|
| `POST` | `/api/auth/register` | `{ username, email, password, bio }` | Регистрация нового пользователя. Возвращает JWT | Public |
| `POST` | `/api/auth/login` | `{ username, password }` | Вход в систему. Возвращает JWT | Public |

### 👤 Users — `/api/users`

| Метод | URL | Описание | Доступ |
|---|---|---|---|
| `GET` | `/api/users/{id}` | Профиль пользователя со статистикой (посты, подписчики, подписки) | AUTH |
| `GET` | `/api/users` | Список всех пользователей | ADMIN |
| `POST` | `/api/users/{id}/follow` | Подписаться на пользователя | AUTH |
| `DELETE` | `/api/users/{id}/follow` | Отписаться от пользователя | AUTH |
| `PUT` | `/api/users/{id}/role` | Изменить роль пользователя | ADMIN |
| `PUT` | `/api/users/{id}/block?blocked=true` | Заблокировать / разблокировать | ADMIN |

### 📝 Posts — `/api/posts`

| Метод | URL | Описание | Доступ |
|---|---|---|---|
| `POST` | `/api/posts` | Создать новый пост | AUTH |
| `GET` | `/api/posts/global?page=0&size=20` | Глобальная лента (все посты, по дате) | AUTH |
| `GET` | `/api/posts/feed?page=0&size=20` | Личная лента (только подписки + свои) | AUTH |
| `DELETE` | `/api/posts/{id}` | Удалить пост (автор / MANAGER / ADMIN) | AUTH |

### 💬 Interactions

| Метод | URL | Описание | Доступ |
|---|---|---|---|
| `POST` | `/api/posts/{postId}/comments` | Добавить комментарий к посту | AUTH |
| `GET` | `/api/posts/{postId}/comments` | Получить все комментарии поста | AUTH |
| `DELETE` | `/api/comments/{commentId}` | Удалить комментарий (автор / MANAGER / ADMIN) | AUTH |
| `POST` | `/api/posts/{postId}/like` | Лайкнуть пост | AUTH |
| `DELETE` | `/api/posts/{postId}/like` | Убрать лайк | AUTH |

> **AUTH** — требуется заголовок `Authorization: Bearer <token>`

---

## Запуск проекта

### Предварительные требования

- **Java 17+** — [скачать](https://adoptium.net/)
- **Maven 3.8+** — [скачать](https://maven.apache.org/)
- **PostgreSQL 14+** — [скачать](https://www.postgresql.org/)

### Шаг 1 — Создание базы данных

```sql
CREATE DATABASE microblog;
```

### Шаг 2 — Настройка подключения

Отредактируйте `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/microblog
    username: postgres        # ваш пользователь PostgreSQL
    password: your_password   # ваш пароль PostgreSQL
```

> Схема БД создаётся автоматически при первом запуске (`ddl-auto: update`).

### Шаг 3 — Сборка и запуск

```bash
# Установить зависимости и собрать проект
mvn clean install

# Запустить приложение
mvn spring-boot:run
```

Приложение запустится на `http://localhost:8080`.

### Шаг 4 — Swagger UI

Откройте в браузере для интерактивного тестирования API:

```
http://localhost:8080/swagger-ui.html
```

Для авторизованных запросов нажмите **Authorize** и введите токен в формате:
```
Bearer <ваш_jwt_токен>
```

---

## Тестирование

```bash
mvn test
```

Реализованы юнит-тесты для `PostService` (`PostServiceTest.java`):

| Тест | Описание |
|---|---|
| ✅ `createPost_Success` | Успешное создание поста |
| ✅ `deletePost_ByAuthor_Success` | Автор удаляет свой пост |
| ✅ `deletePost_ByNonAuthor_ThrowsAccessDenied` | `AccessDeniedException` при удалении чужого поста |
| ✅ `deletePost_ByManager_Success` | MANAGER может удалить любой пост |
| ✅ `deletePost_NotFound_ThrowsResourceNotFound` | `ResourceNotFoundException` для несуществующего поста |

Фреймворк: **JUnit 5 + Mockito** (`@ExtendWith(MockitoExtension.class)`).

---

## Обработка ошибок

Все ошибки возвращаются в едином JSON-формате:

```json
{
  "timestamp": "2026-05-18 12:00:00",
  "status": 404,
  "message": "Post not found with id: 99"
}
```

| HTTP статус | Когда возникает |
|---|---|
| `400 Bad Request` | Ошибки валидации входных данных |
| `401 Unauthorized` | Отсутствует или невалидный JWT |
| `403 Forbidden` | Нет прав на выполнение операции |
| `404 Not Found` | Запрашиваемый ресурс не найден |
| `409 Conflict` | Дублирующийся ресурс (повторный лайк, подписка и т. д.) |
| `500 Internal Server Error` | Непредвиденная ошибка сервера |

Централизованная обработка реализована в `GlobalExceptionHandler` (`@RestControllerAdvice`).
