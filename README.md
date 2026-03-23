# Электронный каталог книг
## REST API проект на Java, фреймворк Spring, Maven.

Electronic Book Catalog — это учебное Spring Boot приложение, представляющее собой REST API для управления каталогом книг. Финальной целью является создание полноценного backend-сервиса с подключением к базе данных, реализующего операции просмотра, поиска, сортировки и управления каталогом книг. На данный момент реализована возможность получения полного каталога книг, запросов по параметру и по id.

1. Реализовать сложный GET-запрос с фильтрацией по вложенной сущности с использованием @Query (JPQL).
2. Реализовать аналогичный запрос через native query.
3. Добавить пагинацию (Pageable).
4. Реализовать in-memory индекс на основе HashMap<K, V> для ранее запрошенных данных. Ключ должен формироваться из параметров запроса (составной ключ). Обеспечить корректную работу индекса за счёт правильной реализации equals() и hashCode().
5. Реализовать инвалидацию индекса при изменении данных.
[Сонар](https://sonarcloud.io/project/overview?id=xenia777666_Electronic-Book-Catalog)

## Сложный GET-запрос с фильтрацией по вложенной сущности с использованием @Query

   GET http://localhost:8080/api/books/search/complex-paginated?genre=Классика&page=1&size=2

## Аналогичный запрос через native query

   GET http://localhost:8080/api/books/search/native-paginated?genre=Классика&page=1&size=2

## Запросы для инвалидации и статуса кэша

   POST http://localhost:8080/api/books/cache/invalidate

   GET http://localhost:8080/api/books/cache/stats

## ER-диаграмма базы данных

```mermaid
erDiagram
    PUBLISHER ||--o{ BOOK : publishes
    BOOK }o--|| PUBLISHER : published-by
    
    BOOK }o--o{ AUTHOR : written-by
    AUTHOR }o--o{ BOOK : writes
    
    BOOK }o--o{ GENRE : categorized-as
    GENRE }o--o{ BOOK : contains
    
    BOOK ||--o{ REVIEW : has
    REVIEW }o--|| BOOK : belongs-to

    PUBLISHER {
        bigint id PK
        string name
        string address
        string phone
        string email
    }

    BOOK {
        bigint id PK
        string isbn
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