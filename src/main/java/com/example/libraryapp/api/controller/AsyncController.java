package com.example.libraryapp.api.controller;

import com.example.libraryapp.service.AsyncTaskService;
import com.example.libraryapp.api.dto.TaskCreatedResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class AsyncController {

    private final AsyncTaskService asyncTaskService;

    @Operation(summary = "Запустить асинхронную бизнес-операцию")
    @ApiResponse(responseCode = "202", description = "Задача принята в обработку")
    @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")

    @PostMapping
    public ResponseEntity<TaskCreatedResponseDto> startTask() {
        // Запускаем задачу - метод возвращает ID СРАЗУ
        String taskId = asyncTaskService.startTask();

        log.info("Задача {} запущена, HTTP ответ отправлен немедленно", taskId);

        // Возвращаем 202 Accepted + ID задачи (НЕ дожидаемся завершения!)
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(new TaskCreatedResponseDto(taskId));
    }

    @Operation(summary = "Проверить статус асинхронной задачи")
    @ApiResponse(responseCode = "200", description = "Статус получен")
    @ApiResponse(responseCode = "404", description = "Задача не найдена")
    @GetMapping("/{taskId}")
    public ResponseEntity<AsyncTaskService.TaskStatus> getTaskStatus(@PathVariable String taskId) {
        AsyncTaskService.TaskStatus status = asyncTaskService.getTaskStatus(taskId);

        if (status == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(status);
    }
}