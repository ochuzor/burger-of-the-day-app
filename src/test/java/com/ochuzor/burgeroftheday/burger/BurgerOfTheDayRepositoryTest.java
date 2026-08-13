package com.ochuzor.burgeroftheday.burger;

import static org.assertj.core.api.Assertions.assertThat;

import com.ochuzor.burgeroftheday.user.User;
import com.ochuzor.burgeroftheday.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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

  @Test
  void visiblePublishedBurgersAreReturnedInRequestedOrder() {
    burgerOfTheDayRepository.deleteAll();
    userRepository.deleteAll();
    entityManager.flush();

    User user = this.userRepository.save(new User("tester", "Tester, M.D."));

    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Burger#1",
            "Visible, published",
            Instant.parse("2026-08-10T12:00:00Z"),
            LocalDate.of(2026, 8, 11),
            user));
    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Burger#2",
            "Visible, published",
            Instant.parse("2026-08-10T12:00:00Z"),
            LocalDate.of(2026, 8, 11),
            user));

    BurgerOfTheDay hiddenBurger =
        burgerOfTheDayRepository.save(
            new BurgerOfTheDay(
                "Burger#3",
                "Hidden, published",
                Instant.parse("2026-08-10T12:00:00Z"),
                LocalDate.of(2026, 8, 11),
                user));

    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Burger#4",
            "Visible, future",
            Instant.parse("2026-08-10T12:00:00Z"),
            LocalDate.of(2099, 8, 11),
            user));

    entityManager.flush();

    entityManager
        .createQuery(
            "update BurgerOfTheDay burger " + "set burger.hidden = true " + "where burger.id = :id")
        .setParameter("id", hiddenBurger.getId())
        .executeUpdate();

    entityManager.clear();

    LocalDate today = LocalDate.of(2026, 8, 13);
    PageRequest pageRequest =
        PageRequest.of(
            0,
            50,
            Sort.by(
                Sort.Order.desc("publishDate"),
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")));

    Page<BurgerOfTheDay> page =
        this.burgerOfTheDayRepository.findByHiddenFalseAndPublishDateLessThanEqual(
            today, pageRequest);

    assertThat(page.getTotalElements()).isEqualTo(2);

    List<String> burgerTexts = page.getContent().stream().map(burger -> burger.getText()).toList();
    assertThat(burgerTexts).containsExactly("Burger#2", "Burger#1");
  }

  @Test
  void visiblePublishedBurgersCanBeFilteredByPublicationDate() {
    burgerOfTheDayRepository.deleteAll();
    userRepository.deleteAll();
    entityManager.flush();

    User user = this.userRepository.save(new User("tester", "Tester, M.D."));

    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Burger#1",
            "burger 1",
            Instant.parse("2026-08-10T12:00:00Z"),
            LocalDate.of(2026, 8, 10),
            user));
    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Burger#2",
            "burger 2",
            Instant.parse("2026-08-10T12:00:00Z"),
            LocalDate.of(2026, 8, 11),
            user));
    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Burger#3",
            "burger 3",
            Instant.parse("2026-08-10T12:00:00Z"),
            LocalDate.of(2026, 8, 12),
            user));

    LocalDate requestedDate = LocalDate.of(2026, 8, 11);
    LocalDate today = LocalDate.of(2026, 8, 13);

    Page<BurgerOfTheDay> page =
        this.burgerOfTheDayRepository
            .findByHiddenFalseAndPublishDateEqualsAndPublishDateLessThanEqual(
                requestedDate, today, PageRequest.of(0, 50));

    assertThat(page.getTotalElements()).isEqualTo(1);
    List<String> burgerTexts = page.getContent().stream().map(burger -> burger.getText()).toList();
    assertThat(burgerTexts).containsExactly("Burger#2");
  }

  @Test
  void futurePublicationDateReturnsEmptyPage() {
    burgerOfTheDayRepository.deleteAll();
    userRepository.deleteAll();
    entityManager.flush();

    User user = this.userRepository.save(new User("tester", "Tester, M.D."));

    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Burger#1",
            "burger 1",
            Instant.parse("2026-08-10T12:00:00Z"),
            LocalDate.of(2099, 8, 11),
            user));

    LocalDate requestedDate = LocalDate.of(2099, 8, 11);
    LocalDate today = LocalDate.of(2026, 8, 13);

    Page<BurgerOfTheDay> page =
        this.burgerOfTheDayRepository
            .findByHiddenFalseAndPublishDateEqualsAndPublishDateLessThanEqual(
                requestedDate, today, PageRequest.of(0, 50));

    assertThat(page.getContent()).isEmpty();
    assertThat(page.getTotalElements()).isZero();
  }
}
