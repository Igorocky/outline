package org.igye.outline2.controllers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@TestConfiguration
public class ComponentTestConfig {
    @Bean
    public Clock testClock() {
        return Clock.fixed(ZonedDateTime.of(
                2019,
                7,
                22,
                9,
                1,
                47,
                0,
                ZoneId.systemDefault()
        ).toInstant(), ZoneId.systemDefault());
    }
}
