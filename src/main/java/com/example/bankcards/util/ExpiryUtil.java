package com.example.bankcards.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public final class ExpiryUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private ExpiryUtil() {
    }

    public static LocalDate toEndOfMonth(String expiryMonth) {
        YearMonth ym = YearMonth.parse(expiryMonth, FORMATTER);
        return ym.atEndOfMonth();
    }
}

