package com.thv.sport.system.common;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for retrieving messages from message properties files
 */
@Component
public class MessageUtils {

    private static MessageSource messageSource;

    public MessageUtils(MessageSource messageSource) {
        MessageUtils.messageSource = messageSource;
    }

    /**
     * Get localized message by message code using current request locale
     * @param messageCode the message key
     * @return localized message
     */
    public static String getMessage(String messageCode) {
        return messageSource.getMessage(
                messageCode,
                null,
                LocaleContextHolder.getLocale()
        );
    }

    /**
     * Get localized message with arguments
     * @param messageCode the message key
     * @param args arguments for placeholders
     * @return localized message
     */
    public static String getMessage(String messageCode, Object... args) {
        return messageSource.getMessage(
                messageCode,
                args,
                LocaleContextHolder.getLocale()
        );
    }
}