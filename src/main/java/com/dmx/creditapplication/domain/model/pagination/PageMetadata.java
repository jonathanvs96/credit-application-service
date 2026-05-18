package com.dmx.creditapplication.domain.model.pagination;

public record PageMetadata(
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean isLast
) {
}
