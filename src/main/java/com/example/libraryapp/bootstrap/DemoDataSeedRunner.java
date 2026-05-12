package com.example.libraryapp.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Integer.MAX_VALUE)
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeedRunner implements ApplicationRunner {

    private final DemoDataSeedService demoDataSeedService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("app.seed.enabled=true — checking demo data...");
        demoDataSeedService.seedIfEmpty();
    }
}
