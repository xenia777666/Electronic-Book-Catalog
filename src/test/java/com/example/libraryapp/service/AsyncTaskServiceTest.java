package com.example.libraryapp.service;

import com.example.libraryapp.service.AsyncTaskService.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncTaskServiceTest {

    private AsyncTaskService asyncTaskService;

    @BeforeEach
    void setUp() {
        asyncTaskService = new AsyncTaskService(null);
        asyncTaskService.setTestMode(true);
    }

    @Test
    void constructor_WithNullSelf_ShouldWork() {
        AsyncTaskService service = new AsyncTaskService(null);
        assertThat(service).isNotNull();
    }

    @Test
    void startTask_ShouldGenerateSequentialTaskIds() {
        String taskId1 = asyncTaskService.startTask();
        String taskId2 = asyncTaskService.startTask();
        String taskId3 = asyncTaskService.startTask();

        assertThat(taskId1).isEqualTo("1");
        assertThat(taskId2).isEqualTo("2");
        assertThat(taskId3).isEqualTo("3");
    }

    @Test
    void startTask_ShouldCreateTaskWithPendingStatus() {
        String taskId = asyncTaskService.startTask();

        TaskStatus status = asyncTaskService.getTaskStatus(taskId);

        assertThat(status)
                .isNotNull()
                .satisfies(s -> {
                    assertThat(s.getStatus()).isEqualTo("PENDING");
                    assertThat(s.getStartTime()).isGreaterThan(0);
                    assertThat(s.getResult()).isNull();
                    assertThat(s.getError()).isNull();
                    assertThat(s.getEndTime()).isZero();
                });
    }

    @Test
    void startTask_WhenTestModeFalse_ShouldCallExecuteTaskAsync() {
        AsyncTaskService realService = new AsyncTaskService(null);
        realService.setTestMode(false);

        String taskId = realService.startTask();

        assertThat(taskId).isNotNull();
        assertThat(realService.getTaskStatus(taskId)).isNotNull();
    }

    @Test
    void executeTaskAsync_WhenTaskNotFound_ShouldDoNothing() {
        asyncTaskService.executeTaskAsync("999");
        assertThat(asyncTaskService.getTaskStatus("999")).isNull();
    }

    @Test
    void executeTaskAsync_WhenTaskExistsAndTestMode_ShouldCompleteSuccessfully() throws Exception {
        String taskId = asyncTaskService.startTask();

        CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
                asyncTaskService.executeTaskAsync(taskId)
        );

        future.get(1, TimeUnit.SECONDS);

        TaskStatus status = asyncTaskService.getTaskStatus(taskId);
        assertThat(status)
                .isNotNull()
                .satisfies(s -> {
                    assertThat(s.getStatus()).isEqualTo("COMPLETED");
                    assertThat(s.getStartTime()).isGreaterThan(0);
                    assertThat(s.getResult()).isEqualTo("Бизнес-операция успешно выполнена");
                    assertThat(s.getEndTime()).isGreaterThan(0);
                });
    }

    @Test
    void executeTaskAsync_WhenInterruptedExceptionOccurs_ShouldSetFailedStatus() throws Exception {
        String taskId = asyncTaskService.startTask();

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            Thread.currentThread().interrupt();
            asyncTaskService.executeTaskAsync(taskId);
        });

        future.get(1, TimeUnit.SECONDS);

        TaskStatus status = asyncTaskService.getTaskStatus(taskId);
        assertThat(status)
                .isNotNull()
                .satisfies(s -> {
                    assertThat(s.getStatus()).isEqualTo("FAILED");
                    assertThat(s.getError()).contains("Задача была прервана");
                    assertThat(s.getEndTime()).isGreaterThan(0);
                });
    }

    @Test
    void getTaskStatus_WhenTaskExists_ShouldReturnStatus() {
        String taskId = asyncTaskService.startTask();
        TaskStatus status = asyncTaskService.getTaskStatus(taskId);
        assertThat(status).isNotNull();
        assertThat(status.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void getTaskStatus_WhenTaskDoesNotExist_ShouldReturnNull() {
        TaskStatus status = asyncTaskService.getTaskStatus("non-existent");
        assertThat(status).isNull();
    }

    @Test
    void getAllTasks_WhenNoTasks_ShouldReturnEmptyMap() {
        Map<String, TaskStatus> tasks = asyncTaskService.getAllTasks();
        assertThat(tasks).isEmpty();
    }

    @Test
    void getAllTasks_WhenTasksExist_ShouldReturnAllTasks() {
        asyncTaskService.startTask();
        asyncTaskService.startTask();
        asyncTaskService.startTask();

        Map<String, TaskStatus> tasks = asyncTaskService.getAllTasks();

        assertThat(tasks)
                .hasSize(3)
                .containsKeys("1", "2", "3");
    }

    @Test
    void cleanOldTasks_WhenNoTasks_ShouldReturnZero() {
        int removed = asyncTaskService.cleanOldTasks();
        assertThat(removed).isZero();
    }

    @Test
    void cleanOldTasks_WhenOnlyPendingTasks_ShouldRemoveNothing() {
        asyncTaskService.startTask();
        asyncTaskService.startTask();

        int removed = asyncTaskService.cleanOldTasks();

        assertThat(removed).isZero();
        assertThat(asyncTaskService.getAllTasks()).hasSize(2);
    }

    @Test
    void cleanOldTasks_WhenCompletedTasksAreOld_ShouldRemoveThem() {
        String taskId = asyncTaskService.startTask();

        TaskStatus status = asyncTaskService.getTaskStatus(taskId);
        status.setStatus("COMPLETED");
        status.setEndTime(System.currentTimeMillis() - 7200000);

        int removed = asyncTaskService.cleanOldTasks();

        assertThat(removed).isEqualTo(1);
        assertThat(asyncTaskService.getTaskStatus(taskId)).isNull();
    }

    @Test
    void cleanOldTasks_WhenFailedTasksAreOld_ShouldRemoveThem() {
        String taskId = asyncTaskService.startTask();

        TaskStatus status = asyncTaskService.getTaskStatus(taskId);
        status.setStatus("FAILED");
        status.setEndTime(System.currentTimeMillis() - 7200000);

        int removed = asyncTaskService.cleanOldTasks();

        assertThat(removed).isEqualTo(1);
        assertThat(asyncTaskService.getTaskStatus(taskId)).isNull();
    }

    @Test
    void cleanOldTasks_WhenCompletedTasksAreRecent_ShouldNotRemoveThem() {
        String taskId = asyncTaskService.startTask();

        TaskStatus status = asyncTaskService.getTaskStatus(taskId);
        status.setStatus("COMPLETED");
        status.setEndTime(System.currentTimeMillis());

        int removed = asyncTaskService.cleanOldTasks();

        assertThat(removed).isZero();
        assertThat(asyncTaskService.getTaskStatus(taskId)).isNotNull();
    }

    @Test
    void taskStatus_Constructor_SetsPendingAndStartTime() {
        TaskStatus status = new TaskStatus();

        assertThat(status)
                .satisfies(s -> {
                    assertThat(s.getStatus()).isEqualTo("PENDING");
                    assertThat(s.getStartTime()).isGreaterThan(0);
                    assertThat(s.getResult()).isNull();
                    assertThat(s.getError()).isNull();
                    assertThat(s.getEndTime()).isZero();
                });
    }

    @Test
    void taskStatus_SetAndGetStatus_WorksCorrectly() {
        TaskStatus status = new TaskStatus();
        status.setStatus("COMPLETED");
        assertThat(status.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void taskStatus_SetAndGetResult_WorksCorrectly() {
        TaskStatus status = new TaskStatus();
        status.setResult("Success");
        assertThat(status.getResult()).isEqualTo("Success");
    }

    @Test
    void taskStatus_SetAndGetError_WorksCorrectly() {
        TaskStatus status = new TaskStatus();
        status.setError("Some error");
        assertThat(status.getError()).isEqualTo("Some error");
    }

    @Test
    void taskStatus_SetAndGetStartTime_WorksCorrectly() {
        TaskStatus status = new TaskStatus();
        long time = 123456789L;
        status.setStartTime(time);
        assertThat(status.getStartTime()).isEqualTo(time);
    }

    @Test
    void taskStatus_SetAndGetEndTime_WorksCorrectly() {
        TaskStatus status = new TaskStatus();
        long time = 123456789L;
        status.setEndTime(time);
        assertThat(status.getEndTime()).isEqualTo(time);
    }

    @Test
    void taskStatus_GetDuration_WhenNotCompleted_ReturnsCurrentDuration() throws InterruptedException {
        TaskStatus status = new TaskStatus();
        Thread.sleep(10);
        long duration = status.getDuration();
        assertThat(duration).isGreaterThanOrEqualTo(10);
    }

    @Test
    void taskStatus_GetDuration_WhenCompleted_ReturnsFixedDuration() {
        TaskStatus status = new TaskStatus();
        long startTime = 1000L;
        long endTime = 5000L;
        status.setStartTime(startTime);
        status.setEndTime(endTime);
        assertThat(status.getDuration()).isEqualTo(4000L);
    }

    @Test
    void taskStatus_GetDuration_WhenEndTimeIsZero_ReturnsCurrentDuration() throws InterruptedException {
        TaskStatus status = new TaskStatus();
        Thread.sleep(10);
        long duration = status.getDuration();
        assertThat(duration).isGreaterThanOrEqualTo(10);
        assertThat(status.getEndTime()).isZero();
    }

    @Test
    void startTask_ThreadSafe_ShouldGenerateUniqueIdsUnderConcurrency() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        String[] taskIds = new String[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            new Thread(() -> {
                taskIds[index] = asyncTaskService.startTask();
                latch.countDown();
            }).start();
        }

        latch.await(5, TimeUnit.SECONDS);

        for (int i = 0; i < threadCount; i++) {
            assertThat(taskIds[i]).isNotNull();
        }

        Map<String, TaskStatus> tasks = asyncTaskService.getAllTasks();
        assertThat(tasks).hasSize(threadCount);
    }

    @Test
    void setTestMode_ShouldChangeTestMode() {
        asyncTaskService.setTestMode(false);
        String taskId = asyncTaskService.startTask();
        assertThat(taskId).isNotNull();

        asyncTaskService.setTestMode(true);
        taskId = asyncTaskService.startTask();
        assertThat(taskId).isNotNull();
    }
}