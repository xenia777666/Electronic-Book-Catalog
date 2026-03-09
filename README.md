# Электронный каталог книг
## REST API проект на Java, фреймворк Spring, Maven.

Electronic Book Catalog — это учебное Spring Boot приложение, представляющее собой REST API для управления каталогом книг. Финальной целью является создание полноценного backend-сервиса с подключением к базе данных, реализующего операции просмотра, поиска, сортировки и управления каталогом книг. На данный момент реализована возможность получения полного каталога книг, запросов по параметру и по id.

1. Подключить реляционную БД к проекту.
2. В модели данных реализовать минимум 5 сущностей:
- минимум одну связь OneToMany
- минимум одну связь ManyToMany
3. Реализовать CRUD операции.
4. Настроить и обосновать использование CascadeType и FetchType.
5. Продемонстрировать проблему N+1 и решить её через @EntityGraph или fetch join.
6. Реализовать метод, сохраняющий несколько связанных сущностей. Продемонстрировать частичное сохранение данных без @Transactional и полное откатывание операции с @Transactional при возникновении ошибки.
7. Нарисовать ER-диаграмму с указанием PK/FK и связей.
[Сонар](https://sonarcloud.io/project/overview?id=xenia777666_Electronic-Book-Catalog)

## Запросы для теста проблемы N+1

   GET http://localhost:8080/api/books/with-n-plus-one
   GET http://localhost:8080/api/books/with-details

## Запрос для теста @Transactional

POST http://localhost:8080/api/books/with-transaction
POST http://localhost:8080/api/books/without-transaction
Content-Type: application/json

{
"isbn": "978-5-699-18031-2",
"title": "error test without transaction",
"price": 500.00,
"publisherId": 1,
"authorIds": [1]
}

## ER-диаграмма базы данных

![ER-диаграмма](images/er.bmp)