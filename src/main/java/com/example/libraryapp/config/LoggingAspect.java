package com.example.libraryapp.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {


    @Pointcut("execution(* com.example.libraryapp.service.*.*(..))")
    public void serviceMethods() {
    }


    @Pointcut("execution(* com.example.libraryapp.api.controller.*.*(..))")
    public void controllerMethods() {
    }


    @Around("serviceMethods()")
    public Object logServiceMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        log.debug("Executing service method: {} with arguments: {}", methodName, Arrays.toString(args));

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;

            if (executionTime > 1000) {
                log.warn("SLOW SERVICE METHOD: {} took {} ms", methodName, executionTime);
            } else {
                log.debug("Service method: {} executed in {} ms", methodName, executionTime);
            }

            return result;
        } catch (Exception e) {
            log.error("Service method: {} threw exception: {}", methodName, e.getMessage(), e);
            throw e;
        }
    }


    @Around("controllerMethods()")
    public Object logControllerMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        log.info("Executing controller method: {} with arguments: {}", methodName, Arrays.toString(args));

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;

            if (executionTime > 500) {
                log.warn("SLOW CONTROLLER METHOD: {} took {} ms", methodName, executionTime);
            } else {
                log.info("Controller method: {} executed in {} ms", methodName, executionTime);
            }

            return result;
        } catch (Exception e) {
            log.error("Controller method: {} threw exception: {}", methodName, e.getMessage(), e);
            throw e;
        }
    }
}