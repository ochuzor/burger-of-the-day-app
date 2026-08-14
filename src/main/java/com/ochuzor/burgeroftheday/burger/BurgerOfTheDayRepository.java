package com.ochuzor.burgeroftheday.burger;

import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BurgerOfTheDayRepository extends JpaRepository<BurgerOfTheDay, Long> {

  Page<BurgerOfTheDay> findByHiddenFalse(Pageable pageable);

  Page<BurgerOfTheDay> findByHiddenFalseAndPublishedAtGreaterThanEqualAndPublishedAtLessThan(
      Instant start, Instant end, Pageable pageable);
}
