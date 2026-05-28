package com.procurement.common.utils;

public final class SearchUtils {

  private SearchUtils() {}

  public static String normalizeSearch(String search) {
    if (search == null || search.isBlank()) return null;
    return search.trim();
  }

  public static String toSearchPattern(String search) {
    return "%" + search.toLowerCase() + "%";
  }
}
