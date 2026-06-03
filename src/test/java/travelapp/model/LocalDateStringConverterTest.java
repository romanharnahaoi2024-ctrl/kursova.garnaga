package travelapp.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class LocalDateStringConverterTest {

    private final LocalDateStringConverter converter = new LocalDateStringConverter();

    @Test
    void testConvertToDatabaseColumn() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertEquals("2026-06-03", converter.convertToDatabaseColumn(LocalDate.of(2026, 6, 3)));
    }

    @Test
    void testConvertToEntityAttribute() {
        assertNull(converter.convertToEntityAttribute(null));
        assertEquals(LocalDate.of(2026, 6, 3), converter.convertToEntityAttribute("2026-06-03"));
    }

    @Test
    void testConvertToEntityAttributeWithEpochMillis() {
        long millis = LocalDate.of(2026, 6, 3)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        assertEquals(LocalDate.of(2026, 6, 3), converter.convertToEntityAttribute(String.valueOf(millis)));
    }

    @Test
    void testConvertToEntityAttributeInvalid() {
        assertThrows(RuntimeException.class, () -> converter.convertToEntityAttribute("invalid-date"));
    }
}
