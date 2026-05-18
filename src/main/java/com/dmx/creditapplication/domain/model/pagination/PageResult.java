package com.dmx.creditapplication.domain.model.pagination;

import lombok.Builder;

import java.util.List;

@Builder
public record PageResult<T>(
    List<T> content,
    PageMetadata metadata
){
}
