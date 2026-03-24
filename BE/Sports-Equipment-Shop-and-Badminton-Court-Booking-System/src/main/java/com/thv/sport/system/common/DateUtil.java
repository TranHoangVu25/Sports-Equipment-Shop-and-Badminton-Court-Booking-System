package com.thv.sport.system.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static String formatToDDMMYYYYHHMMSS(LocalDateTime dateTime) {
        if (dateTime == null) return null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
        return dateTime.format(formatter);
    }

    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.now();
        String result = formatToDDMMYYYYHHMMSS(now);
        System.out.println(result); // ví dụ: 25032026153045
    }
}
