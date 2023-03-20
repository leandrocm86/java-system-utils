package lcm.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import lcm.java.system.Filer;
import lcm.java.system.Props;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PropsTest {

    static final String TEST_FILE = "test.properties";
    static final String[] TEST_PROPERTIES = new String[] {
        "name = Leo",
        "age = 36",
        "height.meters = 1.75",
        "male=true"
    };

    @Test
    @AfterAll
    static void clean() {
        Filer.deleteIfExists(TEST_FILE);
    }

    @Test
    @Order(1)
    void testLoad() {
        Filer.getOrCreate(TEST_FILE).write(Arrays.asList(TEST_PROPERTIES));
        Props.load(TEST_FILE);
        assertEquals("Leo", Props.getString("name"));
        assertEquals("36", Props.getString("age"));
        assertEquals("1.75", Props.getString("height.meters"));
        assertEquals("true", Props.getString("male"));
    }

    @Test
    @Order(2)
    void testGetsOK() {
        assertEquals("Leo", Props.getString("name"));
        assertEquals(36, Props.getInt("age"));
        assertEquals(1.75, Props.getDouble("height.meters"));
        assertEquals(true, Props.getBoolean("male"));
    }

    @Test
    @Order(2)
    void testGetsDefaults() {
        assertEquals("elf", Props.getString("race", "elf"));
        assertEquals(0, Props.getInt("projects", 0));
        assertEquals(null, Props.getDouble("height.inches", null));
        assertEquals(false, Props.getBoolean("female", false));
    }

    @Test
    @Order(2)
    void testGetsFails() {
        assertThrows(IllegalArgumentException.class, () -> Props.getString("race"));
        assertThrows(IllegalArgumentException.class, () -> Props.getInt("projects"));
        assertThrows(IllegalArgumentException.class, () -> Props.getDouble("height.inches"));
        assertThrows(IllegalArgumentException.class, () -> Props.getBoolean("female"));
    }

    @Test
    @Order(3)
    void testSets() {
        Props.setString("name", "Mary");
        Props.setInt("age", 42);
        Props.setDouble("height.meters", 1.62);
        Props.setBoolean("male", false);
        Props.setBoolean("female", true);
        assertEquals("Mary", Props.getString("name"));
        assertEquals(42, Props.getInt("age"));
        assertEquals(1.62, Props.getDouble("height.meters"));
        assertEquals(false, Props.getBoolean("male"));
        assertEquals(true, Props.getBoolean("female"));
    }

    @Test
    @Order(4)
    void testSave() {
        assertFalse(Filer.get(TEST_FILE).read().contains("Mary"));
        Props.save();
        assertTrue(Filer.get(TEST_FILE).read().contains("Mary"));
    }

    @Test
    @Order(5)
    void testSaveAutosave() {
        Props.setAutoSave(true);
        assertFalse(Filer.get(TEST_FILE).read().contains("Vanessa"));
        Props.setString("name", "Vanessa");
        assertTrue(Filer.get(TEST_FILE).read().contains("Vanessa"));
    }


}
