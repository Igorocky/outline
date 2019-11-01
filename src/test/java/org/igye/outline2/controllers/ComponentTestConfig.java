package org.igye.outline2.controllers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@TestConfiguration
public class ComponentTestConfig {

    public static final ZonedDateTime FIXED_DATE_TIME = ZonedDateTime.of(
            2019,
            7,
            22,
            9,
            1,
            47,
            0,
            ZoneId.systemDefault()
    );

    @Bean
    public TestClock testClock() {
        return new TestClock(FIXED_DATE_TIME);
    }
}
