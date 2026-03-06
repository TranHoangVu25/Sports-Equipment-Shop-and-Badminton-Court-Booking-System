package com.thv.sport.system.common;

/**
 * Utility class for retrieving messages from message codes
 */
public class MessageUtils {

    /**
     * Get message from message code
     * @param messageCode the message code
     * @return the message corresponding to the code
     */
    public static String getMessage(String messageCode) {
        // This can be extended to use ResourceBundle or message properties files
        // For now, return the code itself as a simple implementation
        return getMessageByCode(messageCode);
    }

    /**
     * Map message codes to messages
     */
    private static String getMessageByCode(String code) {
        return switch (code) {
            case "SUCCESS" -> "Operation successful";
            case "SUCCESS.CREATE" -> "Created successfully";
            case "SUCCESS.UPDATE" -> "Updated successfully";
            case "SUCCESS.DELETE" -> "Deleted successfully";
            case "SUCCESS.FETCH" -> "Fetched successfully";

            case "ERROR.VALIDATION" -> "Validation failed";
            case "ERROR.NOT_FOUND" -> "Resource not found";
            case "ERROR.UNAUTHORIZED" -> "Unauthorized access";
            case "ERROR.FORBIDDEN" -> "Access forbidden";
            case "ERROR.INTERNAL" -> "Internal server error";
            case "ERROR.DUPLICATE" -> "Duplicate resource";

            default -> code;
        };
    }
}

