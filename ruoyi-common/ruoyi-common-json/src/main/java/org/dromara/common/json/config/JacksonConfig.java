package org.dromara.common.json.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import org.dromara.common.json.handler.BigNumberSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * jackson 配置
 *
 * @author Lion Li
 */
@Slf4j
@AutoConfiguration(before = JacksonAutoConfiguration.class)
@AllArgsConstructor
public class JacksonConfig {
    private final MessageSource messageSource;
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            JavaTimeModule javaTimeModule = new JavaTimeModule();

            // Long / BigDecimal 保持你原来的配置
            javaTimeModule.addSerializer(Long.class, BigNumberSerializer.INSTANCE);
            javaTimeModule.addSerializer(Long.TYPE, BigNumberSerializer.INSTANCE);
            javaTimeModule.addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE);
            javaTimeModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);

            // LocalDateTime 动态国际化
            javaTimeModule.addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    Locale locale = LocaleContextHolder.getLocale();
                    String pattern = messageSource.getMessage("datetime.format", null, locale);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale);
                    gen.writeString(value.format(formatter));
                }
            });

            // LocalDate
            javaTimeModule.addSerializer(LocalDate.class, new JsonSerializer<LocalDate>() {
                @Override
                public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    Locale locale = LocaleContextHolder.getLocale();
                    String pattern = messageSource.getMessage("datetime.format", null, locale);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale);
                    gen.writeString(value.format(formatter));
                }
            });

            // Date
            javaTimeModule.addSerializer(Date.class, new JsonSerializer<Date>() {
                @Override
                public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    Locale locale = LocaleContextHolder.getLocale();
                    String pattern = messageSource.getMessage("datetime.format", null, locale);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale);
                    LocalDateTime ldt = value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    gen.writeString(ldt.format(formatter));
                }
            });

            builder.modules(javaTimeModule);
            builder.timeZone(TimeZone.getDefault());

            log.info("初始化 jackson 国际化配置");
        };
    }

}
