package travelapp.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDate;

@Converter(autoApply = true)
public class LocalDateStringConverter implements AttributeConverter<LocalDate, String> {

    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            // If the database has a numeric string (milliseconds) due to a previous run, fall back safely
            if (dbData.matches("^\\d+$")) {
                long epochMillis = Long.parseLong(dbData);
                return java.time.Instant.ofEpochMilli(epochMillis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
            }
            return LocalDate.parse(dbData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse LocalDate value: " + dbData, e);
        }
    }
}
