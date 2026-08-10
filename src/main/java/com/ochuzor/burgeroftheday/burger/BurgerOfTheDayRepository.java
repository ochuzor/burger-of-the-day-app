package com.ochuzor.burgeroftheday.burger;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BurgerOfTheDayRepository extends JpaRepository<BurgerOfTheDay, Long> {}
