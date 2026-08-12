package com.ochuzor.burgeroftheday.burger;

import com.ochuzor.burgeroftheday.user.UnknownUserException;
import com.ochuzor.burgeroftheday.user.User;
import com.ochuzor.burgeroftheday.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BurgerOfTheDayService {
  private final BurgerOfTheDayRepository burgerOfTheDayRepository;
  private final UserRepository userRepository;
  private final Clock clock;

  public BurgerOfTheDayService(
      BurgerOfTheDayRepository burgerOfTheDayRepository,
      UserRepository userRepository,
      Clock clock) {
    this.burgerOfTheDayRepository = burgerOfTheDayRepository;
    this.userRepository = userRepository;
    this.clock = clock;
  }

  @Transactional
  public Long createBurgerOfTheDay(
      String text, String commentary, LocalDate publishDate, String username) {
    User creator =
        this.userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UnknownUserException("user not found"));

    LocalDate today = LocalDate.now(this.clock);
    if (publishDate.isBefore(today)) {
      throw new PastPublicationDateException("Date: " + publishDate + " is in the past");
    }

    Instant createdAt = Instant.now(clock);
    BurgerOfTheDay burgerOfTheDay =
        new BurgerOfTheDay(text, commentary, createdAt, publishDate, creator);
    BurgerOfTheDay savedBurgerOfTheDay = this.burgerOfTheDayRepository.save(burgerOfTheDay);
    return savedBurgerOfTheDay.getId();
  }

  @Transactional(readOnly = true)
  public PublishedBurgerOfTheDayResponse getPublishedBurgerOfTheDay(long id) {
    BurgerOfTheDay burger =
        this.burgerOfTheDayRepository
            .findById(id)
            .orElseThrow(() -> new BurgerOfTheDayNotFoundException());

    LocalDate today = LocalDate.now(this.clock);
    if (burger.isHidden() || burger.getPublishDate().isAfter(today)) {
      throw new BurgerOfTheDayNotFoundException();
    }

    PublishedBurgerOfTheDayResponse responseBurger =
        new PublishedBurgerOfTheDayResponse(
            burger.getId(),
            burger.getText(),
            burger.getCommentary(),
            burger.getPublishDate(),
            burger.getCreator().getUsername());

    return responseBurger;
  }
}
