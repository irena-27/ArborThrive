package com.blackrock.challenge.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {
  private DateTimeUtil() {}

  public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public static LocalDateTime parse(String s) {
    return LocalDateTime.parse(s, FORMATTER);
  }

  public static String format(LocalDateTime dt) {
    return dt.format(FORMATTER);
  }
}
