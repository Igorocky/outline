package org.igye.outline2.controllers;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;

public class TestClock extends Clock {
    private ZonedDateTime fixedTime;

    public TestClock(ZonedDateTime fixedTime) {
        this.fixedTime = fixedTime;
    }

    public void setFixedTime(ZonedDateTime fixedTime) {
        this.fixedTime = fixedTime;
    }

    public void plus(long amountToAdd, TemporalUnit unit) {
        this.fixedTime = fixedTime.plus(amountToAdd, unit);
    }

    public void setFixedTime(Instant instant) {
        this.fixedTime = ZonedDateTime.ofInstant(instant, fixedTime.getZone());
    }

    public void setFixedTime(int year, int month, int dayOfMonth,
                             int hour, int minute, int second) {
        this.fixedTime = ZonedDateTime.of(
                year, month, dayOfMonth, hour, minute, second, 0, ZoneId.of("UTC")
        );
    }

    @Override
    public ZoneId getZone() {
        return fixedTime.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new TestClock(fixedTime.withZoneSameInstant(zone));
    }

    @Override
    public Instant instant() {
        return fixedTime.toInstant();
    }
}
