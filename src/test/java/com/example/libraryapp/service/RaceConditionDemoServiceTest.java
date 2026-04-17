package com.example.libraryapp.service;

import com.example.libraryapp.service.RaceConditionDemoService.RaceConditionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RaceConditionDemoServiceTest {

    private RaceConditionDemoService raceConditionDemoService;

    @BeforeEach
    void setUp() {
        raceConditionDemoService = new RaceConditionDemoService();
    }

    @Test
    void demonstrateRaceCondition_ShouldReturnNonNullResult() {
        RaceConditionResult result = raceConditionDemoService.demonstrateRaceCondition(50, 100);
        assertThat(result).isNotNull();
    }

    @Test
    void demonstrateRaceCondition_ShouldUseMin50Threads() {
        RaceConditionResult result = raceConditionDemoService.demonstrateRaceCondition(10, 100);
        assertThat(result.getNumberOfThreads()).isEqualTo(50);
        assertThat(result.getExpectedValue()).isEqualTo(5000);
    }

    @Test
    void demonstrateRaceCondition_With100Threads_ShouldReturnCorrectStructure() {
        RaceConditionResult result = raceConditionDemoService.demonstrateRaceCondition(100, 500);

        assertThat(result).isNotNull();
        assertThat(result.getNumberOfThreads()).isEqualTo(100);
        assertThat(result.getIncrementsPerThread()).isEqualTo(500);
        assertThat(result.getExpectedValue()).isEqualTo(50000);
        assertThat(result.getSynchronizedLoss()).isZero();
        assertThat(result.getAtomicLoss()).isZero();
    }

    @Test
    void demonstrateRaceCondition_WithZeroIncrements_ShouldReturnZero() {
        RaceConditionResult result = raceConditionDemoService.demonstrateRaceCondition(50, 0);

        assertThat(result.getExpectedValue()).isZero();
        assertThat(result.getUnsafeCounterValue()).isZero();
        assertThat(result.getSynchronizedCounterValue()).isZero();
        assertThat(result.getAtomicCounterValue()).isZero();
    }

    @Test
    void raceConditionResult_AllFields_ShouldBeAccessible() {
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
    void demonstrateRaceCondition_With200Threads_ShouldComplete() {
        RaceConditionResult result = raceConditionDemoService.demonstrateRaceCondition(200, 200);

        assertThat(result.getNumberOfThreads()).isEqualTo(200);
        assertThat(result.getExpectedValue()).isEqualTo(40000);
    }

    @Test
    void demonstrateRaceCondition_With500Threads_ShouldNotThrowException() {
        RaceConditionResult result = raceConditionDemoService.demonstrateRaceCondition(500, 100);

        assertThat(result).isNotNull();
        assertThat(result.getNumberOfThreads()).isEqualTo(500);
    }

    @Test
    void demonstrateRaceCondition_SynchronizedCounter_ShouldAlwaysMatchExpected() {
        RaceConditionResult result = raceConditionDemoService.demonstrateRaceCondition(100, 500);

        assertThat(result.getSynchronizedCounterValue()).isEqualTo(result.getExpectedValue());
    }

    @Test
    void demonstrateRaceCondition_AtomicCounter_ShouldAlwaysMatchExpected() {
        RaceConditionResult result = raceConditionDemoService.demonstrateRaceCondition(100, 500);

        assertThat(result.getAtomicCounterValue()).isEqualTo(result.getExpectedValue());
    }

    @Test
    void raceConditionResult_DefaultValues_ShouldBeZero() {
        RaceConditionResult result = new RaceConditionResult();

        assertThat(result.getExpectedValue()).isZero();
        assertThat(result.getUnsafeCounterValue()).isZero();
        assertThat(result.getSynchronizedCounterValue()).isZero();
        assertThat(result.getAtomicCounterValue()).isZero();
        assertThat(result.getNumberOfThreads()).isZero();
        assertThat(result.getIncrementsPerThread()).isZero();
        assertThat(result.getUnsafeLoss()).isZero();
        assertThat(result.getSynchronizedLoss()).isZero();
        assertThat(result.getAtomicLoss()).isZero();
    }

    @Test
    void demonstrateRaceCondition_WithLargeIncrements_ShouldNotOverflow() {
        RaceConditionResult result = raceConditionDemoService.demonstrateRaceCondition(50, 100000);

        assertThat(result.getExpectedValue()).isEqualTo(5_000_000);
        assertThat(result.getSynchronizedCounterValue()).isEqualTo(5_000_000);
    }

    @Test
    void demonstrateRaceCondition_WithMinimumParameters_ShouldWork() {
        RaceConditionResult result = raceConditionDemoService.demonstrateRaceCondition(1, 1);

        assertThat(result.getNumberOfThreads()).isEqualTo(50);
        assertThat(result.getExpectedValue()).isEqualTo(50);
    }

    @Test
    void demonstrateRaceCondition_ShouldHaveSynchronizedCounterCorrect() {
        RaceConditionResult result = raceConditionDemoService.demonstrateRaceCondition(100, 1000);

        assertThat(result.getSynchronizedCounterValue())
                .as("Synchronized counter should match expected value")
                .isEqualTo(result.getExpectedValue());
    }

    @Test
    void demonstrateRaceCondition_ShouldHaveAtomicCounterCorrect() {
        RaceConditionResult result = raceConditionDemoService.demonstrateRaceCondition(100, 1000);

        assertThat(result.getAtomicCounterValue())
                .as("Atomic counter should match expected value")
                .isEqualTo(result.getExpectedValue());
    }

    @Test
    void demonstrateRaceCondition_UnsafeCounter_MayHaveLossOrNot() {
        RaceConditionResult result = raceConditionDemoService.demonstrateRaceCondition(100, 1000);

        assertThat(result.getUnsafeLoss()).isGreaterThanOrEqualTo(0);
        assertThat(result.getUnsafeCounterValue()).isLessThanOrEqualTo(result.getExpectedValue());
    }

    @Test
    void demonstrateRaceCondition_MultipleCalls_ShouldReturnValidResults() {
        RaceConditionResult result1 = raceConditionDemoService.demonstrateRaceCondition(100, 500);
        RaceConditionResult result2 = raceConditionDemoService.demonstrateRaceCondition(100, 500);

        assertThat(result1.getExpectedValue()).isEqualTo(result2.getExpectedValue());
        assertThat(result1.getSynchronizedCounterValue()).isEqualTo(result2.getSynchronizedCounterValue());
        assertThat(result1.getAtomicCounterValue()).isEqualTo(result2.getAtomicCounterValue());
    }
}