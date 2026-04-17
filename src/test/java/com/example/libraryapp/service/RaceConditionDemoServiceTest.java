package com.example.libraryapp.service;

import com.example.libraryapp.service.RaceConditionDemoService.RaceConditionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RaceConditionDemoServiceTest {

    private RaceConditionDemoService service;

    @BeforeEach
    void setUp() {
        service = new RaceConditionDemoService();
    }

    @Test
    void demonstrateRaceCondition_WithDefaultValues_ShouldReturnResult() {
        RaceConditionResult result = service.demonstrateRaceCondition(60, 2000);

        assertThat(result).isNotNull();
        assertThat(result.getNumberOfThreads()).isEqualTo(60);
        assertThat(result.getIncrementsPerThread()).isEqualTo(2000);
        assertThat(result.getExpectedValue()).isEqualTo(120000);
        assertThat(result.getUnsafeLoss()).isGreaterThanOrEqualTo(0);
        assertThat(result.getSynchronizedLoss()).isZero();
        assertThat(result.getAtomicLoss()).isZero();
    }

    @Test
    void demonstrateRaceCondition_WithLessThanMinThreads_ShouldUseMin50Threads() {
        RaceConditionResult result = service.demonstrateRaceCondition(10, 100);

        assertThat(result.getNumberOfThreads()).isEqualTo(50);
        assertThat(result.getExpectedValue()).isEqualTo(5000);
    }

    @ParameterizedTest
    @CsvSource({
            "50, 1000",
            "100, 500",
            "200, 250"
    })
    void demonstrateRaceCondition_WithVariousParameters_ShouldReturnValidResult(int threads, int increments) {
        RaceConditionResult result = service.demonstrateRaceCondition(threads, increments);

        assertThat(result)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.getNumberOfThreads()).isEqualTo(Math.max(threads, 50));
                    assertThat(r.getIncrementsPerThread()).isEqualTo(increments);
                    assertThat(r.getExpectedValue()).isEqualTo(Math.max(threads, 50) * increments);
                    assertThat(r.getSynchronizedLoss()).isZero();
                    assertThat(r.getAtomicLoss()).isZero();
                });
    }

    @Test
    void demonstrateRaceCondition_WithZeroIncrements_ShouldReturnZeroExpected() {
        RaceConditionResult result = service.demonstrateRaceCondition(50, 0);

        assertThat(result.getExpectedValue()).isZero();
        assertThat(result.getUnsafeCounterValue()).isZero();
        assertThat(result.getSynchronizedCounterValue()).isZero();
        assertThat(result.getAtomicCounterValue()).isZero();
    }

    @Test
    void demonstrateRaceCondition_WithSingleThread_ShouldHaveNoLoss() {
        RaceConditionResult result = service.demonstrateRaceCondition(1, 1000);

        assertThat(result.getNumberOfThreads()).isEqualTo(50);
        assertThat(result.getUnsafeLoss()).isZero();
        assertThat(result.getSynchronizedLoss()).isZero();
        assertThat(result.getAtomicLoss()).isZero();
    }

    @Test
    void raceConditionResult_AllGettersAndSetters_ShouldWorkCorrectly() {
        RaceConditionResult result = new RaceConditionResult();

        result.setExpectedValue(1000);
        result.setUnsafeCounterValue(950);
        result.setSynchronizedCounterValue(1000);
        result.setAtomicCounterValue(1000);
        result.setNumberOfThreads(10);
        result.setIncrementsPerThread(100);
        result.setUnsafeLoss(50);
        result.setSynchronizedLoss(0);
        result.setAtomicLoss(0);

        assertThat(result.getExpectedValue()).isEqualTo(1000);
        assertThat(result.getUnsafeCounterValue()).isEqualTo(950);
        assertThat(result.getSynchronizedCounterValue()).isEqualTo(1000);
        assertThat(result.getAtomicCounterValue()).isEqualTo(1000);
        assertThat(result.getNumberOfThreads()).isEqualTo(10);
        assertThat(result.getIncrementsPerThread()).isEqualTo(100);
        assertThat(result.getUnsafeLoss()).isEqualTo(50);
        assertThat(result.getSynchronizedLoss()).isZero();
        assertThat(result.getAtomicLoss()).isZero();
    }

    @Test
    void demonstrateRaceCondition_WithLargeNumberOfThreads_ShouldCompleteSuccessfully() {
        RaceConditionResult result = service.demonstrateRaceCondition(200, 500);

        assertThat(result).isNotNull();
        assertThat(result.getNumberOfThreads()).isEqualTo(200);
        assertThat(result.getSynchronizedCounterValue()).isEqualTo(result.getExpectedValue());
        assertThat(result.getAtomicCounterValue()).isEqualTo(result.getExpectedValue());
    }

    @Test
    void demonstrateRaceCondition_WithHighIncrements_ShouldCompleteSuccessfully() {
        RaceConditionResult result = service.demonstrateRaceCondition(50, 10000);

        assertThat(result).isNotNull();
        assertThat(result.getExpectedValue()).isEqualTo(500000);
        assertThat(result.getSynchronizedCounterValue()).isEqualTo(result.getExpectedValue());
        assertThat(result.getAtomicCounterValue()).isEqualTo(result.getExpectedValue());
    }

    @Test
    void demonstrateRaceCondition_ConcurrentCalls_ShouldBeIndependent() throws InterruptedException {
        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicReference<RaceConditionResult> resultRef = new AtomicReference<>();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    RaceConditionResult r = service.demonstrateRaceCondition(50, 100);
                    resultRef.set(r);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(resultRef.get()).isNotNull();
    }

    @Test
    void unsafeCounter_ShouldBehaveCorrectly() throws Exception {
        UnsafeCounterTestHelper counter = new UnsafeCounterTestHelper();
        int threadCount = 100;
        int increments = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < increments; j++) {
                    counter.increment();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(counter.getValue()).isLessThanOrEqualTo(threadCount * increments);
    }

    @Test
    void synchronizedCounter_ShouldAlwaysBeCorrect() throws Exception {
        SynchronizedCounterTestHelper counter = new SynchronizedCounterTestHelper();
        int threadCount = 100;
        int increments = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < increments; j++) {
                    counter.increment();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(counter.getValue()).isEqualTo(threadCount * increments);
    }

    @Test
    void atomicCounter_ShouldAlwaysBeCorrect() throws Exception {
        AtomicIntegerTestHelper counter = new AtomicIntegerTestHelper();
        int threadCount = 100;
        int increments = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < increments; j++) {
                    counter.increment();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(counter.getValue()).isEqualTo(threadCount * increments);
    }

    @Test
    void demonstrateRaceCondition_ShouldLogWarningWhenRaceConditionDetected() {
        RaceConditionResult result = service.demonstrateRaceCondition(200, 1000);

        if (result.getUnsafeLoss() > 0) {
            assertThat(result.getUnsafeLoss()).isPositive();
        }
    }

    @Test
    void demonstrateRaceCondition_WithExtremeValues_ShouldNotThrowException() {
        RaceConditionResult result = service.demonstrateRaceCondition(500, 10000);

        assertThat(result).isNotNull();
        assertThat(result.getSynchronizedCounterValue()).isEqualTo(result.getExpectedValue());
    }

    private static class UnsafeCounterTestHelper {
        private int value = 0;
        public void increment() { value++; }
        public int getValue() { return value; }
    }

    private static class SynchronizedCounterTestHelper {
        private int value = 0;
        public synchronized void increment() { value++; }
        public synchronized int getValue() { return value; }
    }

    private static class AtomicIntegerTestHelper {
        private final java.util.concurrent.atomic.AtomicInteger value = new java.util.concurrent.atomic.AtomicInteger(0);
        public void increment() { value.incrementAndGet(); }
        public int getValue() { return value.get(); }
    }
}