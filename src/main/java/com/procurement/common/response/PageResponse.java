package com.procurement.common.response;

import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public record PageResponse<T>(List<T> data, Meta meta, Links links) {
  public static <T> PageResponse<T> from(Page<T> page) {
    return new PageResponse<>(
      page.getContent(),
      new Meta(
        page.getSize(),
        page.getTotalElements(),
        page.getNumber() + 1,
        page.getTotalPages(),
        sortBy(page.getSort()),
        Collections.emptyList(),
        "",
        Collections.emptyList()
      ),
      new Links(
        page.getNumber() > 0 ? pageLink(1, page.getSize()) : null,
        page.hasPrevious() ? pageLink(page.getNumber(), page.getSize()) : null,
        pageLink(page.getNumber() + 1, page.getSize()),
        page.hasNext() ? pageLink(page.getNumber() + 2, page.getSize()) : null,
        page.getTotalPages() > 0 ? pageLink(page.getTotalPages(), page.getSize()) : null
      )
    );
  }

  public record Meta(
    int itemsPerPage,
    long totalItems,
    int currentPage,
    int totalPages,
    List<List<String>> sortBy,
    List<String> searchBy,
    String search,
    List<String> select
  ) {}

  public record Links(String first, String previous, String current, String next, String last) {}

  private static List<List<String>> sortBy(Sort sort) {
    return sort
      .stream()
      .map(order -> List.of(order.getProperty(), order.getDirection().name()))
      .toList();
  }

  private static String pageLink(int page, int size) {
    return "?page=" + page + "&limit=" + size;
  }
}
