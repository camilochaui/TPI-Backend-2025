package org.example.servicioenvios.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    private static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
            JavaTimeModule module = new JavaTimeModule();
            module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
            module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));

            builder.modules(module);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            // also set a simple date format for legacy Date types
            builder.simpleDateFormat(DATETIME_FORMAT);
        };
    }
}
