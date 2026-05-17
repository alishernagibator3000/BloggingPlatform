# 🐦 Microblog Platform — Backend

Полноценный REST API бэкенд для платформы микроблогинга (аналог Twitter/Threads).

## Технологии

| Технология | Назначение |
|---|---|
| Java 17 | Язык программирования |
| Spring Boot 3.3.5 | Фреймворк |
| Spring Security + JWT | Аутентификация и авторизация |
| Spring Data JPA | Работа с базой данных |
| PostgreSQL | СУБД |
| Lombok | Сокращение boilerplate-кода |
| Springdoc OpenAPI | Swagger UI документация |
| JUnit 5 + Mockito | Тестирование |

## Архитектура

```
com.microblog
├── config/          # Security, JWT, OpenAPI конфигурация
├── controllers/     # REST API контроллеры
├── dto/
│   ├── request/     # Входные DTO
│   └── response/    # Выходные DTO
├── entities/        # JPA сущности
├── exceptions/      # Кастомные исключения + GlobalExceptionHandler
├── repositories/    # Spring Data JPA репозитории
└── services/
    └── impl/        # Реализации сервисов
```

## Сущности БД

- **User** — пользователи (username, email, password, bio, role, blocked)
- **Post** — посты (content, user, createdAt)
- **Comment** — комментарии (text, post, user)
- **Subscription** — подписки (follower ↔ followed)
- **Like** — лайки (user ↔ post)

## Роли и права доступа

| Действие | USER | MANAGER | ADMIN |
|---|:---:|:---:|:---:|
| Читать ленты | ✅ | ✅ | ✅ |
| Создавать посты/комментарии | ✅ | ✅ | ✅ |
| Лайкать/подписываться | ✅ | ✅ | ✅ |
| Удалять свои посты/комментарии | ✅ | ✅ | ✅ |
| Удалять любые посты/комментарии | ❌ | ✅ | ✅ |
| Список всех пользователей | ❌ | ❌ | ✅ |
| Менять роли пользователей | ❌ | ❌ | ✅ |
| Блокировать пользователей | ❌ | ❌ | ✅ |

## API Endpoints

### Auth
| Метод | URL | Описание |
|---|---|---|
| POST | `/api/auth/register` | Регистрация (возвращает JWT) |
| POST | `/api/auth/login` | Логин (возвращает JWT) |

### Users
| Метод | URL | Описание |
|---|---|---|
| GET | `/api/users/{id}` | Профиль пользователя со статистикой |
| GET | `/api/users` | Список всех пользователей (ADMIN) |
| POST | `/api/users/{id}/follow` | Подписаться |
| DELETE | `/api/users/{id}/follow` | Отписаться |
| PUT | `/api/users/{id}/role` | Изменить роль (ADMIN) |
| PUT | `/api/users/{id}/block?blocked=true` | Заблокировать/разблокировать (ADMIN) |

### Posts
| Метод | URL | Описание |
|---|---|---|
| POST | `/api/posts` | Создать пост |
| GET | `/api/posts/global?page=0&size=20` | Глобальная лента (Pageable) |
| GET | `/api/posts/feed?page=0&size=20` | Личная лента подписок (Pageable) |
| DELETE | `/api/posts/{id}` | Удалить пост |

### Interactions
| Метод | URL | Описание |
|---|---|---|
| POST | `/api/posts/{postId}/comments` | Добавить комментарий |
| GET | `/api/posts/{postId}/comments` | Получить комментарии поста |
| DELETE | `/api/comments/{commentId}` | Удалить комментарий |
| POST | `/api/posts/{postId}/like` | Лайкнуть пост |
| DELETE | `/api/posts/{postId}/like` | Убрать лайк |

## Запуск проекта

### Предварительные требования
1. **Java 17+** установлена
2. **PostgreSQL** запущен
3. Создана база данных:
   ```sql
   CREATE DATABASE microblog;
   ```

### Настройка
Отредактируйте `src/main/resources/application.yml` при необходимости:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/microblog
    username: postgres
    password: postgres
```

### Сборка и запуск
```bash
# Сборка
mvn clean install

# Запуск
mvn spring-boot:run
```

### Swagger UI
После запуска откройте в браузере:
```
http://localhost:8080/swagger-ui.html
```

## Тестирование
```bash
mvn test
```

Реализованы юнит-тесты для `PostService`:
- ✅ Успешное создание поста
- ✅ Успешное удаление поста автором
- ✅ `AccessDeniedException` при удалении чужого поста
- ✅ Менеджер может удалить любой пост
- ✅ `ResourceNotFoundException` для несуществующего поста

## Обработка ошибок

Все ошибки возвращаются в формате:
```json
{
  "timestamp": "2026-05-17 18:00:00",
  "status": 404,
  "message": "Post not found with id: 99"
}
```

Поддерживаемые HTTP статусы: `400`, `401`, `403`, `404`, `409`, `500`.
