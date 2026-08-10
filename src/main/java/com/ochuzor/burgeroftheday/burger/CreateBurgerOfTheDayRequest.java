package com.ochuzor.burgeroftheday.burger;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateBurgerOfTheDayRequest(
    @NotBlank(message = "Text must not be blank")
        @Size(max = 150, message = "Text must not be longer than 150 characters")
        String text,
    @Size(max = 500, message = "Commentary must not be longer than 500 characters")
        String commentary,
    @JsonProperty("publish_date") @NotNull(message = "Must provide a publish date")
        LocalDate publishDate) {}
