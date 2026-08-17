package com.ochuzor.burgeroftheday.burger;

import static org.assertj.core.api.Assertions.assertThat;

import com.ochuzor.burgeroftheday.user.User;
import com.ochuzor.burgeroftheday.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
            "Spicy Burger", "Comes with spices", Instant.parse("2026-08-10T12:00:00Z"), savedUser);
    BurgerOfTheDay savedBurgerOfTheDay = this.burgerOfTheDayRepository.save(burgerOfTheDay);
    assertThat(savedBurgerOfTheDay.getId()).isNotNull();

    entityManager.flush();
    entityManager.clear();
    BurgerOfTheDay foundBurgerOfTheDay =
        this.burgerOfTheDayRepository.findById(savedBurgerOfTheDay.getId()).orElseThrow();

    assertThat(foundBurgerOfTheDay.getText()).isEqualTo("Spicy Burger");
    assertThat(foundBurgerOfTheDay.getCommentary()).isEqualTo("Comes with spices");
    assertThat(foundBurgerOfTheDay.getPublishedAt())
        .isEqualTo(Instant.parse("2026-08-10T12:00:00Z"));
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
            "Burger#1", "Visible, published", Instant.parse("2026-08-10T12:00:00Z"), user));
    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Burger#2", "Visible, published", Instant.parse("2026-08-10T12:00:00Z"), user));

    BurgerOfTheDay hiddenBurger =
        burgerOfTheDayRepository.save(
            new BurgerOfTheDay(
                "Burger#3", "Hidden, published", Instant.parse("2026-08-10T12:00:00Z"), user));

    entityManager.flush();

    entityManager
        .createQuery(
            "update BurgerOfTheDay burger " + "set burger.hidden = true " + "where burger.id = :id")
        .setParameter("id", hiddenBurger.getId())
        .executeUpdate();

    entityManager.clear();

    PageRequest pageRequest =
        PageRequest.of(0, 50, Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id")));

    Page<BurgerOfTheDay> page = this.burgerOfTheDayRepository.findByHiddenFalse(pageRequest);

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
        new BurgerOfTheDay("Burger#1", "burger 1", Instant.parse("2026-08-10T12:00:00Z"), user));
    burgerOfTheDayRepository.save(
        new BurgerOfTheDay("Burger#2", "burger 2", Instant.parse("2026-08-11T12:00:00Z"), user));
    burgerOfTheDayRepository.save(
        new BurgerOfTheDay("Burger#3", "burger 3", Instant.parse("2026-08-12T12:00:00Z"), user));

    LocalDate requestedDate = LocalDate.of(2026, 8, 11);

    Instant start = requestedDate.atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant end = requestedDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

    Page<BurgerOfTheDay> page =
        this.burgerOfTheDayRepository
            .findByHiddenFalseAndPublishedAtGreaterThanEqualAndPublishedAtLessThan(
                start, end, PageRequest.of(0, 50));

    assertThat(page.getTotalElements()).isEqualTo(1);
    List<String> burgerTexts = page.getContent().stream().map(burger -> burger.getText()).toList();
    assertThat(burgerTexts).containsExactly("Burger#2");
  }

  @Test
  void visibleBurgersCanBeFilteredByCreator() {
    burgerOfTheDayRepository.deleteAll();
    userRepository.deleteAll();
    entityManager.flush();

    User alice = this.userRepository.save(new User("alice", "Tester, Alice"));
    User bob = this.userRepository.save(new User("bob", "Tester, Bob"));

    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Alice's Burger#1", "Alice's burger 1", Instant.parse("2026-08-10T12:00:00Z"), alice));
    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Alice's Burger#2", "Alice's burger 2", Instant.parse("2026-08-11T12:00:00Z"), alice));
    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Bob's Burger", "Bob's burger :D", Instant.parse("2026-08-11T12:00:00Z"), bob));

    PageRequest pageRequest =
        PageRequest.of(0, 50, Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id")));

    entityManager.flush();
    entityManager.clear();

    Page<BurgerOfTheDay> page =
        this.burgerOfTheDayRepository.findByHiddenFalseAndCreatorUsername("alice", pageRequest);

    assertThat(page.getTotalElements()).isEqualTo(2);
    List<String> burgerTexts = page.getContent().stream().map(burger -> burger.getText()).toList();
    assertThat(burgerTexts).containsExactly("Alice's Burger#2", "Alice's Burger#1");
  }

  @Test
  void visibleBurgersCanBeFilteredByCreatorAndPublicationDate() {
    burgerOfTheDayRepository.deleteAll();
    userRepository.deleteAll();
    entityManager.flush();

    User alice = this.userRepository.save(new User("alice", "Tester, Alice"));
    User bob = this.userRepository.save(new User("bob", "Tester, Bob"));

    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Alice's Burger#1", "Alice's burger 1", Instant.parse("2026-08-10T12:00:00Z"), alice));
    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Alice's Burger#2", "Alice's burger 2", Instant.parse("2026-08-11T12:00:00Z"), alice));
    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Bob's Burger", "Bob's burger :D", Instant.parse("2026-08-11T12:00:00Z"), bob));

    entityManager.flush();
    entityManager.clear();

    PageRequest pageRequest =
        PageRequest.of(0, 50, Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id")));

    LocalDate requestedDate = LocalDate.of(2026, 8, 11);
    Instant start = requestedDate.atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant end = requestedDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

    Page<BurgerOfTheDay> page =
        this.burgerOfTheDayRepository
            .findByHiddenFalseAndCreatorUsernameAndPublishedAtGreaterThanEqualAndPublishedAtLessThan(
                "alice", start, end, pageRequest);

    assertThat(page.getTotalElements()).isEqualTo(1);
    List<String> burgerTexts = page.getContent().stream().map(burger -> burger.getText()).toList();
    assertThat(burgerTexts).containsExactly("Alice's Burger#2");
  }
}
