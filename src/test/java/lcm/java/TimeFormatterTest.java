package lcm.java;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import lcm.java.system.TimeFormatter;

public class TimeFormatterTest {

    // 05/01/1986 12:30:30
    private static final LocalDateTime TEST_LDT = LocalDateTime.of(1986, 1, 5, 12, 30, 30);
    private static final long TEST_SECONDS = TEST_LDT.atZone(ZoneId.systemDefault()).toEpochSecond();
    private static final long TEST_MILLIS = TEST_SECONDS * 1000;

    @Test
    void testMillisToLocal() {
        var localDateTime = TimeFormatter.millisToLocal(TEST_MILLIS + 555);
        assertEquals(1986, localDateTime.getYear());
        assertEquals(1, localDateTime.getMonthValue());
        assertEquals(5, localDateTime.getDayOfMonth());
        assertEquals(12, localDateTime.getHour());
        assertEquals(30, localDateTime.getMinute());
        assertEquals(555000000, localDateTime.getNano());
    }

    @Test
    void testSecondsToLocal() {
        var localDateTime = TimeFormatter.secondsToLocal(TEST_SECONDS);
        assertEquals(1986, localDateTime.getYear());
        assertEquals(1, localDateTime.getMonthValue());
        assertEquals(5, localDateTime.getDayOfMonth());
        assertEquals(12, localDateTime.getHour());
        assertEquals(30, localDateTime.getMinute());
    }

    @Test
    void testStringToLocal() {
        var localDateTime = new TimeFormatter("dd/MM/yyyy HH:mm:ss").stringToLocal("05/01/1986 12:30:10");
        assertEquals(1986, localDateTime.getYear());
        assertEquals(1, localDateTime.getMonthValue());
        assertEquals(5, localDateTime.getDayOfMonth());
        assertEquals(12, localDateTime.getHour());
        assertEquals(30, localDateTime.getMinute());
        assertEquals(10, localDateTime.getSecond());
    }

    @Test
    void testLocalToEpoch() {
        var localDateTime = LocalDateTime.of(1986, 1, 5, 12, 30, 30);
        assertEquals(TEST_MILLIS, TimeFormatter.localToMillis(localDateTime));
        assertEquals(TEST_SECONDS, TimeFormatter.localToSeconds(localDateTime));
    }

    @Test
    void testLocalToString() {
        var localDateTime = LocalDateTime.of(1986, 1, 5, 12, 30, 30);
        assertEquals("05/01/1986 12:30:30", new TimeFormatter("dd/MM/yyyy HH:mm:ss").localToString(localDateTime));
    }

    @Test
    void testEpochToString() {
        assertEquals(TEST_SECONDS, new TimeFormatter("dd/MM/yyyy HH:mm:ss").stringToSeconds("05/01/1986 12:30:30"));
        assertEquals(TEST_MILLIS + 555, new TimeFormatter("dd/MM/yyyy HH:mm:ss.SSS").stringToMillis("05/01/1986 12:30:30.555"));
    }

    @Test
    void testStringToEpoch() {
        assertEquals("05/01/1986 12:30:30", new TimeFormatter("dd/MM/yyyy HH:mm:ss").secondsToString(TEST_SECONDS));
        assertEquals("05/01/1986 12:30:30.555", new TimeFormatter("dd/MM/yyyy HH:mm:ss.SSS").millisToString(TEST_MILLIS + 555));
    }

}
