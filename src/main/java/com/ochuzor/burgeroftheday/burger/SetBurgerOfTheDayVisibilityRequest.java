package com.ochuzor.burgeroftheday.burger;

import jakarta.validation.constraints.NotNull;

public record SetBurgerOfTheDayVisibilityRequest(
    @NotNull(message = "hidden is required") Boolean hidden) {}
