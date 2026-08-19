package com.ochuzor.burgeroftheday.burger;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record CreatorBurgerOfTheDayResponse(
    Long id,
    String text,
    String commentary,
    @JsonProperty("published_at") Instant publishedAt,
    @JsonProperty("created_by") String createdBy,
    boolean hidden) {}
