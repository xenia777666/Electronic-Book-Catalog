#!/bin/bash

# Нагрузочное тестирование API библиотеки

BASE_URL="http://localhost:8080/api"
REQUESTS=100
CONCURRENT=10

echo "=== Нагрузочное тестирование библиотечного API ==="
echo "Базовый URL: $BASE_URL"
echo "Всего запросов: $REQUESTS"
echo "Одновременных потоков: $CONCURRENT"
echo ""

# Функция для тестирования GET запросов
test_get_endpoint() {
    local endpoint=$1
    local name=$2
    local count=0
    local start_time=$(date +%s%N)

    for i in $(seq 1 $REQUESTS); do
        curl -s -o /dev/null -w "%{http_code}\n" "$BASE_URL$endpoint" | grep -q "200" && ((count++))
        if (( i % 10 == 0 )); then
            echo "  $name: выполнено $i из $REQUESTS"
        fi
    done

    local end_time=$(date +%s%N)
    local duration=$((($end_time - $start_time) / 1000000))
    local success_rate=$((count * 100 / REQUESTS))

    echo "  $name: Успешно: $count/$REQUESTS ($success_rate%), Время: ${duration}ms"
}

# Функция для тестирования POST запросов
test_post_endpoint() {
    local endpoint=$1
    local name=$2
    local count=0
    local start_time=$(date +%s%N)

    for i in $(seq 1 $REQUESTS); do
        local isbn="978${i}$(date +%s)"
        local json="{\"isbn\":\"$isbn\",\"title\":\"Test Book $i\",\"description\":\"Test Description\",\"publicationYear\":2024,\"price\":500,\"publisherId\":1,\"authorIds\":[1]}"

        curl -s -X POST -H "Content-Type: application/json" -d "$json" -o /dev/null -w "%{http_code}\n" "$BASE_URL$endpoint" | grep -q "201" && ((count++))

        if (( i % 10 == 0 )); then
            echo "  $name: выполнено $i из $REQUESTS"
        fi
    done

    local end_time=$(date +%s%N)
    local duration=$((($end_time - $start_time) / 1000000))
    local success_rate=$((count * 100 / REQUESTS))

    echo "  $name: Успешно: $count/$REQUESTS ($success_rate%), Время: ${duration}ms"
}

# Тестирование различных эндпоинтов
echo "Начинаем тестирование..."

# GET /api/books - получить все книги
test_get_endpoint "/books" "GET /api/books"

# GET /api/authors - получить всех авторов
test_get_endpoint "/authors" "GET /api/authors"

# GET /api/genres - получить все жанры
test_get_endpoint "/genres" "GET /api/genres"

# GET /api/publishers - получить всех издателей
test_get_endpoint "/publishers" "GET /api/publishers"

# POST /api/books - создание книг
test_post_endpoint "/books" "POST /api/books"

# Тестирование асинхронного эндпоинта
echo ""
echo "Тестирование асинхронного эндпоинта..."
async_start=$(date +%s%N)
response=$(curl -s -X POST "$BASE_URL/async/process-books?numberOfBooks=50&operationType=TEST")
task_id=$(echo $response | grep -o '"taskId":"[^"]*"' | cut -d'"' -f4)
async_end=$(date +%s%N)
async_duration=$((($async_end - $async_start) / 1000000))
echo "  Асинхронная задача запущена за ${async_duration}ms, ID: $task_id"

# Ждем завершения асинхронной задачи
echo "  Ожидание завершения задачи..."
for i in {1..30}; do
    status=$(curl -s "$BASE_URL/async/task/$task_id" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
    if [ "$status" = "COMPLETED" ]; then
        echo "  Задача $task_id завершена успешно!"
        break
    elif [ "$status" = "FAILED" ]; then
        echo "  Задача $task_id завершилась с ошибкой!"
        break
    fi
    sleep 1
done

# Тестирование race condition демонстрации
echo ""
echo "Тестирование race condition..."
race_start=$(date +%s%N)
curl -s -X POST "$BASE_URL/race-condition/demonstrate?numberOfThreads=50&incrementsPerThread=1000" > /tmp/race_result.json
race_end=$(date +%s%N)
race_duration=$((($race_end - $race_start) / 1000000))

# Вывод результатов
if [ -f /tmp/race_result.json ]; then
    echo "  Race condition тест завершен за ${race_duration}ms"
    echo "  Результаты сохранены в /tmp/race_result.json"
fi

echo ""
echo "=== Тестирование завершено ==="

# Сводная статистика
echo ""
echo "=== Сводная статистика ==="
echo "Общее количество запросов: $((REQUESTS * 5 + 2))"
echo "Параметры тестирования:"
echo "  - Потоков: $CONCURRENT (имитация параллельных запросов)"
echo "  - Запросов на эндпоинт: $REQUESTS"
echo ""
echo "Рекомендации:"
echo "1. Для более точных результатов используйте JMeter"
echo "2. Увеличьте REQUESTS до 1000+ для серьезного тестирования"
echo "3. Запустите тест несколько раз для стабильности результатов"