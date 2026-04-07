# Электронный каталог книг
## REST API проект на Java, фреймворк Spring, Maven.

Electronic Book Catalog — это учебное Spring Boot приложение, представляющее собой REST API для управления каталогом книг. Финальной целью является создание полноценного backend-сервиса с подключением к базе данных, реализующего операции просмотра, поиска, сортировки и управления каталогом книг. На данный момент реализована возможность получения полного каталога книг, различных запросов и реализована in-memory индекс на основе HashMap<K, V>.

1. Реализовать bulk-операцию (POST со списком объектов), имеющую бизнес-смысл в рамках проекта.
2. Использовать Stream API и Optional в сервисном слое.
3. Обеспечить транзакционность bulk-операции. Продемонстрировать работу с/без @Transactional и показать разницу в состоянии БД.
4. Написать:
- unit-тесты для сервисов (Mockito)
[Сонар](https://sonarcloud.io/project/overview?id=xenia777666_Electronic-Book-Catalog)
[Swagger](http://localhost:8080/swagger-ui/index.html#/)

##  Успешная bulk-операция

   POST http://localhost:8080/api/books/bulk [
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

## Bulk-операция (ошибка) без транзакции

   POST http://localhost:8080/api/books/bulk/without-transaction
   [ { "isbn": "9785012344444", "title": "Первая книга", "price": 500.00, "publisherId": 1, "authorIds": [1] }, { "isbn": "9785042222222", "title": "Вторая книга (дубликат)", "price": 500.00, "publisherId": 1, "authorIds": [1] } ]
 
## Bulk-операция (ошибка) с транзакцией

   POST http://localhost:8080/api/books/bulk/with-transaction
   [ { "isbn": "9785012344444", "title": "Первая книга", "price": 500.00, "publisherId": 1, "authorIds": [1] }, { "isbn": "9785042222222", "title": "Вторая книга (дубликат)", "price": 500.00, "publisherId": 1, "authorIds": [1] } ]

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