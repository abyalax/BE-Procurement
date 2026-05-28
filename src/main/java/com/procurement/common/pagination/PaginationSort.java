package com.procurement.common.pagination;

import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationSort {

  private PaginationSort() {}

  public static Pageable pageRequest(
    int page,
    int size,
    String sortBy,
    String sortOrder,
    Map<String, String> allowedSorts,
    String defaultSortBy,
    Sort.Direction defaultDirection
  ) {
    return PageRequest.of(
      normalizedPage(page),
      normalizedSize(size),
      sort(sortBy, sortOrder, allowedSorts, defaultSortBy, defaultDirection)
    );
  }

  public static Sort sort(
    String sortBy,
    String sortOrder,
    Map<String, String> allowedSorts,
    String defaultSortBy,
    Sort.Direction defaultDirection
  ) {
    String property = resolveSortProperty(sortBy, allowedSorts, defaultSortBy);
    Sort.Direction direction = resolveDirection(sortOrder, defaultDirection);
    return Sort.by(direction, property);
  }

  private static int normalizedPage(int page) {
    return Math.max(page - 1, 0);
  }

  private static int normalizedSize(int size) {
    return Math.min(Math.max(size, 1), 100);
  }

  private static Sort.Direction resolveDirection(
    String sortOrder,
    Sort.Direction defaultDirection
  ) {
    if (sortOrder == null || sortOrder.isBlank()) return defaultDirection;
    try {
      return Sort.Direction.fromString(sortOrder);
    } catch (IllegalArgumentException ignored) {
      return defaultDirection;
    }
  }

  private static String resolveSortProperty(
    String sortBy,
    Map<String, String> allowedSorts,
    String defaultSortBy
  ) {
    String defaultProperty = allowedSorts.getOrDefault(defaultSortBy, defaultSortBy);
    if (sortBy == null || sortBy.isBlank()) return defaultProperty;

    String normalizedSortBy = sortBy.trim();
    String property = allowedSorts.get(normalizedSortBy);
    if (property != null) return property;

    property = allowedSorts.get(toCamelCase(normalizedSortBy));
    return property == null ? defaultProperty : property;
  }

  private static String toCamelCase(String value) {
    StringBuilder result = new StringBuilder();
    boolean upperNext = false;
    for (char character : value.toCharArray()) {
      if (character == '_' || character == '-') {
        upperNext = true;
        continue;
      }
      result.append(upperNext ? Character.toUpperCase(character) : character);
      upperNext = false;
    }
    return result.toString();
  }
}
