package com.ochuzor.burgeroftheday.burger;

import com.ochuzor.burgeroftheday.user.User;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BurgerOfTheDayRepository extends JpaRepository<BurgerOfTheDay, Long> {

  Page<BurgerOfTheDay> findByHiddenFalse(Pageable pageable);

  Page<BurgerOfTheDay> findByHiddenFalseAndPublishedAtGreaterThanEqualAndPublishedAtLessThan(
      Instant start, Instant end, Pageable pageable);

  Page<BurgerOfTheDay> findByHiddenFalseAndCreatorUsername(String username, Pageable pageable);

  Page<BurgerOfTheDay>
      findByHiddenFalseAndCreatorUsernameAndPublishedAtGreaterThanEqualAndPublishedAtLessThan(
          String username, Instant start, Instant end, Pageable pageable);

  Page<BurgerOfTheDay> findByCreator(User creator, Pageable pageable);

  Page<BurgerOfTheDay> findByCreatorAndPublishedAtGreaterThanEqualAndPublishedAtLessThan(
      User creator, Instant start, Instant end, Pageable pageable);
}
