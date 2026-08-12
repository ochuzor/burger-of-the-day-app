package com.ochuzor.burgeroftheday.burger;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record PublishedBurgerOfTheDayResponse(
    Long id,
    String text,
    String commentary,
    @JsonProperty("publish_date") LocalDate publishDate,
    @JsonProperty("created_by") String createdBy) {}
