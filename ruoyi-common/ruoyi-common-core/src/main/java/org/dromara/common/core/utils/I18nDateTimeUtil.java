package org.dromara.common.core.utils;

import jakarta.annotation.Resource;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/2 9:35
 * @Content
 */

@Component
public class I18nDateTimeUtil {

    @Resource
    private MessageSource messageSource;

    public String format(LocalDateTime time, Locale locale) {
        if (time == null) return null;

        String pattern = messageSource.getMessage("datetime.format", null, locale);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return time.format(formatter);
    }
}
