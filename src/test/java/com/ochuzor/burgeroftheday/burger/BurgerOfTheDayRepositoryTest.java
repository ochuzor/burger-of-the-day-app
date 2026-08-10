package com.ochuzor.burgeroftheday.burger;

import static org.assertj.core.api.Assertions.assertThat;

import com.ochuzor.burgeroftheday.user.User;
import com.ochuzor.burgeroftheday.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BurgerOfTheDayRepositoryTest {
  private final BurgerOfTheDayRepository burgerOfTheDayRepository;
  private final UserRepository userRepository;
  private final EntityManager entityManager;

  @Autowired
  BurgerOfTheDayRepositoryTest(
      BurgerOfTheDayRepository burgerOfTheDayRepository,
      UserRepository userRepository,
      EntityManager entityManager) {
    this.burgerOfTheDayRepository = burgerOfTheDayRepository;
    this.userRepository = userRepository;
    this.entityManager = entityManager;
  }

  @Test
  void savedBurgerRetainsItsCreator() {
    User user = new User("tester", "Tester, M.D.");
    User savedUser = this.userRepository.save(user);

    assertThat(savedUser.getId()).isNotNull();

    BurgerOfTheDay burgerOfTheDay =
        new BurgerOfTheDay(
            "Spicy Burger",
            "Comes with spices",
            Instant.parse("2026-08-10T12:00:00Z"),
            LocalDate.of(2026, 8, 11),
            savedUser);
    BurgerOfTheDay savedBurgerOfTheDay = this.burgerOfTheDayRepository.save(burgerOfTheDay);
    assertThat(savedBurgerOfTheDay.getId()).isNotNull();

    entityManager.flush();
    entityManager.clear();
    BurgerOfTheDay foundBurgerOfTheDay =
        this.burgerOfTheDayRepository.findById(savedBurgerOfTheDay.getId()).orElseThrow();

    assertThat(foundBurgerOfTheDay.getText()).isEqualTo("Spicy Burger");
    assertThat(foundBurgerOfTheDay.getCommentary()).isEqualTo("Comes with spices");
    assertThat(foundBurgerOfTheDay.getPublishDate()).isEqualTo(LocalDate.of(2026, 8, 11));
    assertThat(foundBurgerOfTheDay.isHidden()).isFalse();
    assertThat(foundBurgerOfTheDay.getCreator().getUsername()).isEqualTo("tester");
  }
}
