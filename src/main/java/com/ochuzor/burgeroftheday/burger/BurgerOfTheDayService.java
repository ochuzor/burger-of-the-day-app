package com.ochuzor.burgeroftheday.burger;

import com.ochuzor.burgeroftheday.user.UnknownUserException;
import com.ochuzor.burgeroftheday.user.User;
import com.ochuzor.burgeroftheday.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  public Long createBurgerOfTheDay(String text, String commentary, String username) {
    User creator =
        this.userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UnknownUserException("user not found"));

    Instant publishedAt = Instant.now(this.clock);
    BurgerOfTheDay burgerOfTheDay = new BurgerOfTheDay(text, commentary, publishedAt, creator);
    BurgerOfTheDay savedBurgerOfTheDay = this.burgerOfTheDayRepository.save(burgerOfTheDay);

    return savedBurgerOfTheDay.getId();
  }

  @Transactional(readOnly = true)
  public PublishedBurgerOfTheDayResponse getPublishedBurgerOfTheDay(long id) {
    BurgerOfTheDay burger =
        this.burgerOfTheDayRepository
            .findById(id)
            .orElseThrow(() -> new BurgerOfTheDayNotFoundException());

    if (burger.isHidden()) {
      throw new BurgerOfTheDayNotFoundException();
    }

    PublishedBurgerOfTheDayResponse responseBurger =
        new PublishedBurgerOfTheDayResponse(
            burger.getId(),
            burger.getText(),
            burger.getCommentary(),
            burger.getPublishedAt(),
            burger.getCreator().getUsername());

    return responseBurger;
  }

  @Transactional(readOnly = true)
  public PublishedBurgerOfTheDayPageResponse getBurgersOfTheDay(
      Optional<LocalDate> publishDate, int page, int size) {

    Pageable pageable =
        PageRequest.of(page, size, Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id")));

    Page<BurgerOfTheDay> burgerPage;
    if (publishDate.isEmpty()) {
      burgerPage = burgerOfTheDayRepository.findByHiddenFalse(pageable);
    } else {
      Instant start = publishDate.get().atStartOfDay(ZoneOffset.UTC).toInstant();
      Instant end = publishDate.get().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

      burgerPage =
          burgerOfTheDayRepository
              .findByHiddenFalseAndPublishedAtGreaterThanEqualAndPublishedAtLessThan(
                  start, end, pageable);
    }

    Page<PublishedBurgerOfTheDayResponse> responsePage =
        burgerPage.map(
            burger ->
                new PublishedBurgerOfTheDayResponse(
                    burger.getId(),
                    burger.getText(),
                    burger.getCommentary(),
                    burger.getPublishedAt(),
                    burger.getCreator().getUsername()));

    return new PublishedBurgerOfTheDayPageResponse(
        responsePage.getContent(),
        responsePage.getNumber(),
        responsePage.getSize(),
        responsePage.getTotalElements(),
        responsePage.getTotalPages());
  }
}
