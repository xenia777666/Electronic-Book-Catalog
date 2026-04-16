package com.example.libraryapp.api.controller;

import com.example.libraryapp.service.RaceConditionDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@Validated
@RestController
@RequestMapping("/api/race-condition")
@RequiredArgsConstructor
public class RaceConditionController {

    private final RaceConditionDemoService raceConditionDemoService;

    @Operation(summary = "Демонстрация race condition и потокобезопасных решений")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Демонстрация выполнена"),
        @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    })
    @GetMapping("/race-demo")
    public ResponseEntity<RaceConditionDemoService.RaceConditionResult> runRaceDemo(
            @RequestParam(defaultValue = "60") @Min(1) int threads,
            @RequestParam(defaultValue = "2000") @Min(1) int incrementsPerThread) {

        log.info("Запуск демонстрации race condition с {} потоками, {} инкрементов на поток",
                threads, incrementsPerThread);

        RaceConditionDemoService.RaceConditionResult result =
                raceConditionDemoService.demonstrateRaceCondition(threads, incrementsPerThread);

        return ResponseEntity.ok(result);
    }
}