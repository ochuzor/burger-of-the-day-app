package com.ochuzor.burgeroftheday.burger;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PublishedBurgerOfTheDayPageResponse(
    List<PublishedBurgerOfTheDayResponse> content,
    int page,
    int size,
    @JsonProperty("total_elements") long totalElements,
    @JsonProperty("total_pages") int totalPages) {}
