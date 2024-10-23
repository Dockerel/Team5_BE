package ojosama.talkak.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class HashConverter {

    private final ObjectMapper objectMapper;

    public HashConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> toMap(Object dto) {
        return objectMapper.convertValue(dto,
            new TypeReference<>() {
            });
    }

    public <T> T FromMap(Map<String, Object> map, Class<T> dtoClass) {
        return objectMapper.convertValue(map, dtoClass);
    }

    public static String fromDateTime(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }

    public static LocalDateTime getDateTime(String stringDate) {
        return stringDate != null ? LocalDateTime.parse(stringDate,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }
}
