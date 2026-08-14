package com.ochuzor.burgeroftheday.burger;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBurgerOfTheDayRequest(
    @NotBlank(message = "text should not be empty")
        @Size(max = 150, message = "Text must not be longer than 150 characters")
        String text,
    @Size(max = 500, message = "Commentary must not be longer than 500 characters")
        String commentary) {}
