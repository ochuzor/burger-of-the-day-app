package com.ochuzor.burgeroftheday.burger;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BurgerOfTheDayRepository extends JpaRepository<BurgerOfTheDay, Long> {
  Page<BurgerOfTheDay> findByHiddenFalseAndPublishDateLessThanEqual(
      LocalDate today, Pageable pageable);

  Page<BurgerOfTheDay> findByHiddenFalseAndPublishDateEqualsAndPublishDateLessThanEqual(
      LocalDate requestedDate, LocalDate today, Pageable pageable);
}
