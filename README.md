# Электронный каталог книг
## REST API проект на Java, Spring Boot, Maven

**Electronic Book Catalog** — учебное Spring Boot приложение, представляющее REST API для управления каталогом книг. Финальная цель: полноценный backend-сервис с подключением к БД, реализующий операции просмотра, поиска, сортировки и управления каталогом книг.

**Текущий статус**: реализованы получение полного каталога, различные запросы, in-memory индекс на основе `HashMap<K, V>`.

## Задачи

1. **Реализовать bulk-операцию** (POST со списком объектов) с бизнес-смыслом
2. **Использовать Stream API и Optional** в сервисном слое
3. **Обеспечить транзакционность** bulk-операции:
   - Продемонстрировать работу **с/без `@Transactional`**
   - Показать разницу в состоянии БД
4. **Написать unit-тесты** для сервисов (Mockito)

- [SonarCloud](https://sonarcloud.io/project/overview?id=xenia777666_Electronic-Book-Catalog)
- [Swagger UI](http://localhost:8080/swagger-ui/index.html#/)

## API endpoints

### ✅ Успешная bulk-операция
```http
POST http://localhost:8080/api/books/bulk
```
**Body**:
```json
[
  {
    "isbn": "9785041111111",
    "title": "Bulk книга 1",
    "price": 500.00,
    "publisherId": 1,
    "authorIds": [1]
  },
  {
    "isbn": "9785042222222",
    "title": "Bulk книга 2",
    "price": 600.00,
    "publisherId": 1,
    "authorIds": [1]
  }
]
```

### ❌ Bulk-операция (ошибка) **без транзакции**
```http
POST http://localhost:8080/api/books/bulk/without-transaction
```
**Body** 
```json
[
  {
    "isbn": "9785012344444",
    "title": "Первая книга",
    "price": 500.00,
    "publisherId": 1,
    "authorIds": [1]
  },
  {
    "isbn": "9785042222222",
    "title": "Вторая книга (дубликат)",
    "price": 500.00,
    "publisherId": 1,
    "authorIds": [1]
  }
]
```

### ❌ Bulk-операция (ошибка) **с транзакцией**
```http
POST http://localhost:8080/api/books/bulk/with-transaction
```
**Body** 
```json
[
  {
    "isbn": "9785012344444",
    "title": "Первая книга",
    "price": 500.00,
    "publisherId": 1,
    "authorIds": [1]
  },
  {
    "isbn": "9785042222222",
    "title": "Вторая книга (дубликат)",
    "price": 500.00,
    "publisherId": 1,
    "authorIds": [1]
  }
]
```

## ER-диаграмма базы данных

```mermaid
erDiagram
    PUBLISHER ||--o{ BOOK : publishes
    BOOK }o--|| PUBLISHER : "published-by"
    
    BOOK }o--o{ AUTHOR : "written-by"
    AUTHOR }o--o{ BOOK : writes
    
    BOOK }o--o{ GENRE : "categorized-as"
    GENRE }o--o{ BOOK : contains
    
    BOOK ||--o{ REVIEW : has
    REVIEW }o--|| BOOK : "belongs-to"

    PUBLISHER {
        bigint id PK
        string name
        string address
        string phone
        string email
    }

    BOOK {
        bigint id PK
        string isbn UK
        string title
        string description
        int publication_year
        decimal price
        bigint publisher_id FK
    }

    AUTHOR {
        bigint id PK
        string name
        string biography
        date birth_date
    }

    GENRE {
        bigint id PK
        string name
        string description
    }

    REVIEW {
        bigint id PK
        string reviewer_name
        int rating
        string comment
        bigint book_id FK
    }
```