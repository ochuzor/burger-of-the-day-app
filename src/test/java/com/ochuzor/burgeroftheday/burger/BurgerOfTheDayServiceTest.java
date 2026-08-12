package com.ochuzor.burgeroftheday.burger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ochuzor.burgeroftheday.user.UnknownUserException;
import com.ochuzor.burgeroftheday.user.User;
import com.ochuzor.burgeroftheday.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BurgerOfTheDayServiceTest {

  @Mock private BurgerOfTheDayRepository burgerOfTheDayRepository;
  @Mock private UserRepository userRepository;

  private Clock clock;
  private BurgerOfTheDayService service;

  @BeforeEach
  void setUp() {
    clock = Clock.fixed(Instant.parse("2026-08-10T12:00:00Z"), ZoneOffset.UTC);

    service = new BurgerOfTheDayService(burgerOfTheDayRepository, userRepository, clock);
  }

  @Test
  void successfullySavesBurgerOfTheDay() {
    // Arrange
    User creator = new User("tester", "Tester");
    when(userRepository.findByUsername("tester")).thenReturn(Optional.of(creator));

    BurgerOfTheDay savedBurger = mock(BurgerOfTheDay.class);
    when(savedBurger.getId()).thenReturn(42L);
    when(burgerOfTheDayRepository.save(any(BurgerOfTheDay.class))).thenReturn(savedBurger);

    LocalDate publishDate = LocalDate.of(2026, 8, 10);

    // Act
    Long id =
        service.createBurgerOfTheDay("Spicy Burger", "Comes with spices", publishDate, "tester");

    // Assert returned result
    assertThat(id).isEqualTo(42L);

    // Capture the entity supplied to save(...)
    ArgumentCaptor<BurgerOfTheDay> burgerCaptor = ArgumentCaptor.forClass(BurgerOfTheDay.class);

    verify(burgerOfTheDayRepository).save(burgerCaptor.capture());

    BurgerOfTheDay burgerToSave = burgerCaptor.getValue();

    // Assert the constructed entity
    assertThat(burgerToSave.getText()).isEqualTo("Spicy Burger");
    assertThat(burgerToSave.getCommentary()).isEqualTo("Comes with spices");
    assertThat(burgerToSave.getCreatedAt()).isEqualTo(Instant.parse("2026-08-10T12:00:00Z"));
    assertThat(burgerToSave.getPublishDate()).isEqualTo(publishDate);
    assertThat(burgerToSave.isHidden()).isFalse();
    assertThat(burgerToSave.getCreator()).isSameAs(creator);

    verify(userRepository).findByUsername("tester");
  }

  @Test
  void unknownUserCannotCreateBurgerOfTheDay() {
    when(userRepository.findByUsername("missing-user")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.createBurgerOfTheDay(
                    "Nothing burger", "a joke burger", LocalDate.now(clock), "missing-user"))
        .isInstanceOf(UnknownUserException.class)
        .hasMessage("user not found");

    verify(burgerOfTheDayRepository, never()).save(any(BurgerOfTheDay.class));
  }

  @Test
  void pastPublicationDateIsRejected() {
    User creator = new User("tester", "Tester");
    when(userRepository.findByUsername("tester")).thenReturn(Optional.of(creator));

    LocalDate publishDate = LocalDate.now(clock).minusDays(1);

    assertThatThrownBy(
            () ->
                service.createBurgerOfTheDay(
                    "Nothing burger", "a joke burger", publishDate, "tester"))
        .isInstanceOf(PastPublicationDateException.class)
        .hasMessage("Date: 2026-08-09 is in the past");

    verify(burgerOfTheDayRepository, never()).save(any(BurgerOfTheDay.class));
  }

  @Test
  void publishedBurgerCanBeRetrieved() {
    User creator = new User("tester", "Tester");

    BurgerOfTheDay savedBurger = mock(BurgerOfTheDay.class);
    when(savedBurger.getId()).thenReturn(42L);
    when(savedBurger.getCreator()).thenReturn(creator);
    when(savedBurger.getText()).thenReturn("Fancy burger");
    when(savedBurger.isHidden()).thenReturn(false);

    LocalDate publishDate = LocalDate.of(2026, 8, 10);

    when(savedBurger.getPublishDate()).thenReturn(publishDate);

    when(burgerOfTheDayRepository.findById(42L)).thenReturn(Optional.of(savedBurger));

    PublishedBurgerOfTheDayResponse response = service.getPublishedBurgerOfTheDay(42L);

    assertThat(response.id()).isEqualTo(42L);
    assertThat(response.text()).isEqualTo("Fancy burger");
    assertThat(response.commentary()).isNull();
    assertThat(response.publishDate()).isEqualTo(publishDate);
    assertThat(response.createdBy()).isEqualTo("tester");
  }

  @Test
  void missingBurgerIsReportedAsNotFound() {
    when(burgerOfTheDayRepository.findById(42L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.getPublishedBurgerOfTheDay(42L))
        .isInstanceOf(BurgerOfTheDayNotFoundException.class);
  }

  @Test
  void hiddenBurgerIsReportedAsNotFound() {
    BurgerOfTheDay savedBurger = mock(BurgerOfTheDay.class);
    when(savedBurger.isHidden()).thenReturn(true);

    when(burgerOfTheDayRepository.findById(42L)).thenReturn(Optional.of(savedBurger));

    assertThatThrownBy(() -> service.getPublishedBurgerOfTheDay(42L))
        .isInstanceOf(BurgerOfTheDayNotFoundException.class);
  }

  @Test
  void futureBurgerIsReportedAsNotFound() {
    BurgerOfTheDay savedBurger = mock(BurgerOfTheDay.class);
    when(savedBurger.isHidden()).thenReturn(false);
    when(savedBurger.getPublishDate()).thenReturn(LocalDate.now(clock).plusDays(1));

    when(burgerOfTheDayRepository.findById(42L)).thenReturn(Optional.of(savedBurger));

    assertThatThrownBy(() -> service.getPublishedBurgerOfTheDay(42L))
        .isInstanceOf(BurgerOfTheDayNotFoundException.class);
  }
}
