package org.igye.outline2.controllers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@TestConfiguration
public class ComponentTestConfig {
    @Bean
    public TestClock testClock() {
        return new TestClock(ZonedDateTime.of(
                2019,
                7,
                22,
                9,
                1,
                47,
                0,
                ZoneId.systemDefault()
        ));
    }
}
