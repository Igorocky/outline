package org.igye.outline2.common;

import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.igye.outline2.common.OutlineUtils.instantToMillis;
import static org.igye.outline2.common.OutlineUtils.millisToDurationStr;
import static org.igye.outline2.common.OutlineUtils.strInstantToMillis;
import static org.igye.outline2.common.OutlineUtils.timestampToMillis;

public class OutlineUtilsTest {
    @Test
    public void millisToDurationStr_produces_expected_results() {
        Instant now = Instant.now();

        Assert.assertEquals(
                "1m",
                millisToDurationStr(
                        instantToMillis(now.plusSeconds(63))
                        - instantToMillis(now)
                )
        );
        Assert.assertEquals(
                "1h 1m",
                millisToDurationStr(
                        instantToMillis(now.plus(1, ChronoUnit.HOURS).plusSeconds(75))
                        - instantToMillis(now)
                )
        );
        Assert.assertEquals(
                "1d 0h",
                millisToDurationStr(
                        instantToMillis(now.plus(1, ChronoUnit.DAYS).plusSeconds(75))
                        - instantToMillis(now)
                )
        );
        Assert.assertEquals(
                "1d 1h",
                millisToDurationStr(
                        instantToMillis(now.plus(1, ChronoUnit.DAYS).plus(119, ChronoUnit.MINUTES))
                        - instantToMillis(now)
                )
        );
        Assert.assertEquals(
                "1M 0d",
                millisToDurationStr(
                        instantToMillis(now.plus(30, ChronoUnit.DAYS).plus(119, ChronoUnit.MINUTES))
                        - instantToMillis(now)
                )
        );
        Assert.assertEquals(
                "1M 4d",
                millisToDurationStr(
                        instantToMillis(now.plus(34, ChronoUnit.DAYS))
                        - instantToMillis(now)
                )
        );
        Assert.assertEquals(
                "- 1M 4d",
                millisToDurationStr(
                        instantToMillis(now)
                        - instantToMillis(now.plus(34, ChronoUnit.DAYS))
                )
        );
        Assert.assertEquals(
                "- 1d 1h",
                millisToDurationStr(
                        instantToMillis(now)
                        - instantToMillis(now.plus(1, ChronoUnit.DAYS).plus(119, ChronoUnit.MINUTES))
                )
        );
        Assert.assertNull(millisToDurationStr(null));
    }

    @Test
    public void instantToMillis_returns_expected_results() {
        Instant instant = Instant.now();
        Assert.assertEquals(
                instant.getEpochSecond()*1000 + instant.getNano()/1000_000,
                instantToMillis(instant).longValue()
        );

        Assert.assertNull(instantToMillis(null));
    }

    @Test
    public void strInstantToMillis_returns_expected_results() {
        Assert.assertEquals(
                1572814824607L,
                strInstantToMillis("2019-11-03T21:00:24.607Z").longValue()
        );

        Assert.assertNull(strInstantToMillis(null));
    }

    @Test
    public void timestampToMillis_returns_expected_results() {
        Assert.assertNull(timestampToMillis(null));
    }

}