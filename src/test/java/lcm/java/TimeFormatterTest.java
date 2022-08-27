package lcm.java;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import lcm.java.system.TimeFormatter;

public class TimeFormatterTest {

    @Test
    void testMillisToLocal() {
        var localDateTime = TimeFormatter.millisToLocal(505319430555L);
        assertEquals(1986, localDateTime.getYear());
        assertEquals(1, localDateTime.getMonthValue());
        assertEquals(5, localDateTime.getDayOfMonth());
        assertEquals(12, localDateTime.getHour());
        assertEquals(30, localDateTime.getMinute());
        assertEquals(555000000, localDateTime.getNano());
    }

    @Test
    void testSecondsToLocal() {
        var localDateTime = TimeFormatter.secondsToLocal(505319430);
        assertEquals(1986, localDateTime.getYear());
        assertEquals(1, localDateTime.getMonthValue());
        assertEquals(5, localDateTime.getDayOfMonth());
        assertEquals(12, localDateTime.getHour());
        assertEquals(30, localDateTime.getMinute());
    }

    @Test
    void testStringToLocal() {
        var localDateTime = new TimeFormatter("dd/MM/yyyy HH:mm").stringToLocal("05/01/1986 12:30");
        assertEquals(1986, localDateTime.getYear());
        assertEquals(1, localDateTime.getMonthValue());
        assertEquals(5, localDateTime.getDayOfMonth());
        assertEquals(12, localDateTime.getHour());
        assertEquals(30, localDateTime.getMinute());
    }

    @Test
    void testLocalToEpoch() {
        var localDateTime = LocalDateTime.of(1986, 1, 5, 12, 30, 30);
        assertEquals(505319430000L, TimeFormatter.localToMillis(localDateTime));
        assertEquals(505319430, TimeFormatter.localToSeconds(localDateTime));
    }

    @Test
    void testLocalToString() {
        var localDateTime = LocalDateTime.of(1986, 1, 5, 12, 30, 30);
        assertEquals("05/01/1986 12:30:30", new TimeFormatter("dd/MM/yyyy HH:mm:ss").localToString(localDateTime));
    }

    @Test
    void testEpochToString() {
        assertEquals(505319430, new TimeFormatter("dd/MM/yyyy HH:mm:ss").stringToSeconds("05/01/1986 12:30:30"));
        assertEquals(505319430555L, new TimeFormatter("dd/MM/yyyy HH:mm:ss.SSS").stringToMillis("05/01/1986 12:30:30.555"));
    }

    @Test
    void testStringToEpoch() {
        assertEquals("05/01/1986 12:30:30", new TimeFormatter("dd/MM/yyyy HH:mm:ss").secondsToString(505319430));
        assertEquals("05/01/1986 12:30:30.555", new TimeFormatter("dd/MM/yyyy HH:mm:ss.SSS").millisToString(505319430555L));
    }
    
}
