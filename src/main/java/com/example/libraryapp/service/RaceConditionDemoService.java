package com.example.libraryapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class RaceConditionDemoService {

    public RaceConditionResult demonstrateRaceCondition(int numberOfThreads, int incrementsPerThread) {
        int actualThreads = Math.max(numberOfThreads, 50);
        log.info("Демонстрация race condition с {} потоками, {} инкрементов на поток",
                actualThreads, incrementsPerThread);

        UnsafeCounter unsafeCounter = new UnsafeCounter();
        SynchronizedCounter synchronizedCounter = new SynchronizedCounter();
        AtomicInteger atomicCounter = new AtomicInteger(0);

        ExecutorService executor = startAllCounters(actualThreads, incrementsPerThread,
                unsafeCounter, synchronizedCounter, atomicCounter);

        waitForCompletion(executor);

        return buildResult(actualThreads, incrementsPerThread,
                unsafeCounter, synchronizedCounter, atomicCounter);
    }

    private ExecutorService startAllCounters(int threads, int increments,
                                             UnsafeCounter unsafe,
                                             SynchronizedCounter sync,
                                             AtomicInteger atomic) {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> runCounterTasks(increments, unsafe, sync, atomic));
        }
        return executor;
    }

    private void runCounterTasks(int increments,
                                 UnsafeCounter unsafe,
                                 SynchronizedCounter sync,
                                 AtomicInteger atomic) {
        for (int j = 0; j < increments; j++) {
            unsafe.increment();
            sync.increment();
            atomic.incrementAndGet();
        }
    }

    private void waitForCompletion(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    private RaceConditionResult buildResult(int threads, int increments,
                                            UnsafeCounter unsafe,
                                            SynchronizedCounter sync,
                                            AtomicInteger atomic) {
        int expectedValue = threads * increments;
        RaceConditionResult result = new RaceConditionResult();
        result.setExpectedValue(expectedValue);
        result.setUnsafeCounterValue(unsafe.getValue());
        result.setSynchronizedCounterValue(sync.getValue());
        result.setAtomicCounterValue(atomic.get());
        result.setNumberOfThreads(threads);
        result.setIncrementsPerThread(increments);
        result.setUnsafeLoss(expectedValue - unsafe.getValue());
        result.setSynchronizedLoss(expectedValue - sync.getValue());
        result.setAtomicLoss(expectedValue - atomic.get());

        log.info("Результаты race condition:");
        log.info("  Ожидаемое значение: {}", expectedValue);
        log.info("  Unsafe counter: {} (потеряно {})", unsafe.getValue(), result.getUnsafeLoss());
        log.info("  Synchronized counter: {} (потеряно {})", sync.getValue(), result.getSynchronizedLoss());
        log.info("  Atomic counter: {} (потеряно {})", atomic.get(), result.getAtomicLoss());

        if (result.getUnsafeLoss() > 0) {
            log.warn("⚠️ RACE CONDITION ОБНАРУЖЕНА! Потеряно {} операций", result.getUnsafeLoss());
        }

        return result;
    }

    private static class UnsafeCounter {
        private int value = 0;
        public void increment() {
            value++; }
        public int getValue() {
            return value; }
    }

    private static class SynchronizedCounter {
        private int value = 0;
        public synchronized void increment() {
            value++; }
        public synchronized int getValue() {
            return value; }
    }

    public static class RaceConditionResult {
        private int expectedValue;
        private int unsafeCounterValue;
        private int synchronizedCounterValue;
        private int atomicCounterValue;
        private int numberOfThreads;
        private int incrementsPerThread;
        private int unsafeLoss;
        private int synchronizedLoss;
        private int atomicLoss;

        public int getExpectedValue() {
            return expectedValue; }
        public void setExpectedValue(int expectedValue) {
            this.expectedValue = expectedValue; }
        public int getUnsafeCounterValue() {
            return unsafeCounterValue; }
        public void setUnsafeCounterValue(int unsafeCounterValue) {
            this.unsafeCounterValue = unsafeCounterValue; }
        public int getSynchronizedCounterValue() {
            return synchronizedCounterValue; }
        public void setSynchronizedCounterValue(int synchronizedCounterValue) {
            this.synchronizedCounterValue = synchronizedCounterValue; }
        public int getAtomicCounterValue() {
            return atomicCounterValue; }
        public void setAtomicCounterValue(int atomicCounterValue) {
            this.atomicCounterValue = atomicCounterValue; }
        public int getNumberOfThreads() {
            return numberOfThreads; }
        public void setNumberOfThreads(int numberOfThreads) {
            this.numberOfThreads = numberOfThreads; }
        public int getIncrementsPerThread() {
            return incrementsPerThread; }
        public void setIncrementsPerThread(int incrementsPerThread) {
            this.incrementsPerThread = incrementsPerThread; }
        public int getUnsafeLoss() {
            return unsafeLoss; }
        public void setUnsafeLoss(int unsafeLoss) {
            this.unsafeLoss = unsafeLoss; }
        public int getSynchronizedLoss() {
            return synchronizedLoss; }
        public void setSynchronizedLoss(int synchronizedLoss) {
            this.synchronizedLoss = synchronizedLoss; }
        public int getAtomicLoss() {
            return atomicLoss; }
        public void setAtomicLoss(int atomicLoss) {
            this.atomicLoss = atomicLoss; }
    }
}