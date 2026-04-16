package com.example.libraryapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class AsyncTaskService {

    private final Map<String, TaskStatus> taskStatuses = new ConcurrentHashMap<>();
    private final AtomicLong taskCounter = new AtomicLong(0);

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";

    @Lazy
    @Autowired
    private AsyncTaskService self;

    /**
     * Запустить асинхронную задачу
     * Возвращает ID задачи СРАЗУ, не дожидаясь выполнения
     */
    public String startTask() {
        // 1. Сразу генерируем ID
        String taskId = String.valueOf(taskCounter.incrementAndGet());

        // 2. Создаем статус PENDING
        TaskStatus status = new TaskStatus();
        status.setStatus(STATUS_PENDING);
        taskStatuses.put(taskId, status);

        log.info("Асинхронная задача {} создана (статус PENDING)", taskId);

        // 3. Запускаем асинхронное выполнение через self (чтобы @Async сработал)
        self.executeTaskAsync(taskId);

        // 4. СРАЗУ возвращаем ID (до завершения задачи!)
        return taskId;
    }

    /**
     * Асинхронное выполнение бизнес-логики
     * Этот метод выполняется в отдельном потоке
     */
    @Async("taskExecutor")
    public void executeTaskAsync(String taskId) {
        TaskStatus status = taskStatuses.get(taskId);

        if (status == null) {
            log.error("Задача {} не найдена", taskId);
            return;
        }

        log.info("Начало выполнения асинхронной задачи {} в потоке {}", taskId, Thread.currentThread().getName());
        status.setStatus(STATUS_RUNNING);
        status.setStartTime(System.currentTimeMillis());

        try {
            // Имитация долгой бизнес-операции (30 секунд)
            Thread.sleep(30000);

            // Успешное завершение
            status.setStatus(STATUS_COMPLETED);
            status.setResult("Бизнес-операция успешно выполнена");
            status.setEndTime(System.currentTimeMillis());

            log.info("Асинхронная задача {} успешно завершена за {} мс",
                    taskId, status.getDuration());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            status.setStatus(STATUS_FAILED);
            status.setError("Задача была прервана: " + e.getMessage());
            status.setEndTime(System.currentTimeMillis());

            log.error("Асинхронная задача {} была прервана", taskId, e);

        } catch (Exception e) {
            status.setStatus(STATUS_FAILED);
            status.setError("Ошибка выполнения: " + e.getMessage());
            status.setEndTime(System.currentTimeMillis());

            log.error("Ошибка в асинхронной задаче {}", taskId, e);
        }
    }

    /**
     * Получить статус задачи по ID
     */
    public TaskStatus getTaskStatus(String taskId) {
        return taskStatuses.get(taskId);
    }

    /**
     * Получить все задачи
     */
    public Map<String, TaskStatus> getAllTasks() {
        return new ConcurrentHashMap<>(taskStatuses);
    }

    /**
     * Очистить старые задачи (старше 1 часа)
     */
    public int cleanOldTasks() {
        long oneHourAgo = System.currentTimeMillis() - 3600000;
        int removed = 0;

        for (Map.Entry<String, TaskStatus> entry : taskStatuses.entrySet()) {
            TaskStatus status = entry.getValue();
            if ((status.getStatus().equals(STATUS_COMPLETED) ||
                    status.getStatus().equals(STATUS_FAILED)) &&
                    status.getEndTime() < oneHourAgo) {
                taskStatuses.remove(entry.getKey());
                removed++;
            }
        }

        log.info("Очищено {} старых задач", removed);
        return removed;
    }

    /**
     * Внутренний класс для хранения статуса задачи
     */
    public static class TaskStatus {
        private String status;
        private String result;
        private String error;
        private long startTime;
        private long endTime;

        public TaskStatus() {
            this.status = STATUS_PENDING;
            this.startTime = System.currentTimeMillis();
        }

        public String getStatus() {
            return status; }
        public void setStatus(String status) {
            this.status = status; }

        public String getResult() {
            return result; }
        public void setResult(String result) {
            this.result = result; }

        public String getError() {
            return error; }
        public void setError(String error) {
            this.error = error; }

        public long getStartTime() {
            return startTime; }
        public void setStartTime(long startTime) {
            this.startTime = startTime; }

        public long getEndTime() {
            return endTime; }
        public void setEndTime(long endTime) {
            this.endTime = endTime; }

        public long getDuration() {
            return endTime > 0 ? endTime - startTime : System.currentTimeMillis() - startTime;
        }
    }
}