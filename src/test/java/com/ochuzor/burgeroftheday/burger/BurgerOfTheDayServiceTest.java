package com.ochuzor.burgeroftheday.burger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ochuzor.burgeroftheday.user.UnknownUserException;
import com.ochuzor.burgeroftheday.user.User;
import com.ochuzor.burgeroftheday.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    // Act
    Long id = service.createBurgerOfTheDay("Spicy Burger", "Comes with spices", "tester");

    // Assert returned result
    assertThat(id).isEqualTo(42L);

    // Capture the entity supplied to save(...)
    ArgumentCaptor<BurgerOfTheDay> burgerCaptor = ArgumentCaptor.forClass(BurgerOfTheDay.class);

    verify(burgerOfTheDayRepository).save(burgerCaptor.capture());

    BurgerOfTheDay burgerToSave = burgerCaptor.getValue();

    // Assert the constructed entity
    assertThat(burgerToSave.getText()).isEqualTo("Spicy Burger");
    assertThat(burgerToSave.getCommentary()).isEqualTo("Comes with spices");
    assertThat(burgerToSave.getPublishedAt()).isEqualTo(Instant.parse("2026-08-10T12:00:00Z"));
    assertThat(burgerToSave.isHidden()).isFalse();
    assertThat(burgerToSave.getCreator()).isSameAs(creator);

    verify(userRepository).findByUsername("tester");
  }

  @Test
  void unknownUserCannotCreateBurgerOfTheDay() {
    when(userRepository.findByUsername("missing-user")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.createBurgerOfTheDay("Nothing burger", "a joke burger", "missing-user"))
        .isInstanceOf(UnknownUserException.class)
        .hasMessage("user not found");

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

    Instant publishedAt = Instant.parse("2026-08-10T12:00:00Z");
    when(savedBurger.getPublishedAt()).thenReturn(publishedAt);

    when(burgerOfTheDayRepository.findById(42L)).thenReturn(Optional.of(savedBurger));

    PublishedBurgerOfTheDayResponse response = service.getPublishedBurgerOfTheDay(42L);

    assertThat(response.id()).isEqualTo(42L);
    assertThat(response.text()).isEqualTo("Fancy burger");
    assertThat(response.commentary()).isNull();
    assertThat(response.publishedAt()).isEqualTo(publishedAt);
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
  void publishedBurgersCanBeListed() {
    User creator = new User("tester", "Tester");

    Instant firstPublishedAt = Instant.parse("2026-08-09T12:00:00Z");

    BurgerOfTheDay burger1 = mock(BurgerOfTheDay.class);
    when(burger1.getId()).thenReturn(41L);
    when(burger1.getText()).thenReturn("First Burger");
    when(burger1.getCommentary()).thenReturn("First commentary");
    when(burger1.getPublishedAt()).thenReturn(firstPublishedAt);
    when(burger1.getCreator()).thenReturn(creator);

    Instant secondPublishedAt = Instant.parse("2026-08-10T12:00:00Z");

    BurgerOfTheDay burger2 = mock(BurgerOfTheDay.class);
    when(burger2.getId()).thenReturn(42L);
    when(burger2.getText()).thenReturn("Second Burger");
    when(burger2.getCommentary()).thenReturn(null);
    when(burger2.getPublishedAt()).thenReturn(secondPublishedAt);
    when(burger2.getCreator()).thenReturn(creator);

    List<BurgerOfTheDay> burgers = List.of(burger1, burger2);

    Pageable pageable =
        PageRequest.of(0, 50, Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id")));

    PageImpl<BurgerOfTheDay> repositoryPage = new PageImpl<>(burgers, pageable, 2);
    when(burgerOfTheDayRepository.findByHiddenFalse(pageable)).thenReturn(repositoryPage);

    PublishedBurgerOfTheDayPageResponse response =
        service.getBurgersOfTheDay(Optional.empty(), Optional.empty(), 0, 50);

    PublishedBurgerOfTheDayResponse first = response.content().get(0);
    assertThat(first.id()).isEqualTo(41L);
    assertThat(first.text()).isEqualTo("First Burger");
    assertThat(first.commentary()).isEqualTo("First commentary");
    assertThat(first.publishedAt()).isEqualTo(firstPublishedAt);
    assertThat(first.createdBy()).isEqualTo("tester");

    PublishedBurgerOfTheDayResponse second = response.content().get(1);
    assertThat(second.id()).isEqualTo(42L);
    assertThat(second.text()).isEqualTo("Second Burger");
    assertThat(second.commentary()).isNull();
    assertThat(second.publishedAt()).isEqualTo(secondPublishedAt);
    assertThat(second.createdBy()).isEqualTo("tester");

    assertThat(response.content()).hasSize(2);
    assertThat(response.page()).isZero();
    assertThat(response.size()).isEqualTo(50);
    assertThat(response.totalElements()).isEqualTo(2);
    assertThat(response.totalPages()).isEqualTo(1);
  }

  @Test
  void publishedBurgersCanBeListedByPublicationDate() {
    User creator = new User("tester", "Tester");

    BurgerOfTheDay burger = mock(BurgerOfTheDay.class);
    when(burger.getId()).thenReturn(41L);
    when(burger.getText()).thenReturn("First Burger");
    when(burger.getCommentary()).thenReturn("First commentary");
    when(burger.getPublishedAt()).thenReturn(Instant.parse("2026-08-09T12:00:00Z"));
    when(burger.getCreator()).thenReturn(creator);

    LocalDate requestedDate = LocalDate.of(2026, 8, 9);

    Instant start = requestedDate.atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant end = requestedDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

    Pageable pageable =
        PageRequest.of(0, 50, Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id")));

    PageImpl<BurgerOfTheDay> repositoryPage = new PageImpl<>(List.of(burger), pageable, 1);

    when(burgerOfTheDayRepository
            .findByHiddenFalseAndPublishedAtGreaterThanEqualAndPublishedAtLessThan(
                start, end, pageable))
        .thenReturn(repositoryPage);

    PublishedBurgerOfTheDayPageResponse response =
        service.getBurgersOfTheDay(Optional.of(requestedDate), Optional.empty(), 0, 50);

    PublishedBurgerOfTheDayResponse first = response.content().get(0);
    assertThat(first.id()).isEqualTo(41L);
    assertThat(first.text()).isEqualTo("First Burger");
    assertThat(first.commentary()).isEqualTo("First commentary");
    assertThat(first.publishedAt()).isEqualTo(Instant.parse("2026-08-09T12:00:00Z"));
    assertThat(first.createdBy()).isEqualTo("tester");

    assertThat(response.content()).hasSize(1);
    assertThat(response.page()).isZero();
    assertThat(response.size()).isEqualTo(50);
    assertThat(response.totalElements()).isEqualTo(1);
    assertThat(response.totalPages()).isEqualTo(1);

    verify(burgerOfTheDayRepository, never()).findByHiddenFalse(any(Pageable.class));
    verify(burgerOfTheDayRepository)
        .findByHiddenFalseAndPublishedAtGreaterThanEqualAndPublishedAtLessThan(
            start, end, pageable);
  }

  @Test
  void creatorCanSetBurgerVisibility() {
    User authenticatedUser = mock(User.class);
    User creator = mock(User.class);

    UUID userId = UUID.fromString("241cda9e-95be-40f8-968b-5bfe8da19cc3");

    when(authenticatedUser.getId()).thenReturn(userId);
    when(creator.getId()).thenReturn(userId);

    when(userRepository.findByUsername("tester")).thenReturn(Optional.of(authenticatedUser));

    BurgerOfTheDay burger = mock(BurgerOfTheDay.class);
    when(burger.getCreator()).thenReturn(creator);

    when(burgerOfTheDayRepository.findById(42L)).thenReturn(Optional.of(burger));

    service.setBurgerOfTheDayVisibility(42L, "tester", true);

    verify(burger).setHidden(true);
    verify(burgerOfTheDayRepository, never()).save(any(BurgerOfTheDay.class));
  }

  @Test
  void unknownUserCannotSetBurgerVisibility() {
    when(userRepository.findByUsername("tester")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.setBurgerOfTheDayVisibility(42L, "tester", true))
        .isInstanceOf(UnknownUserException.class);
    verifyNoInteractions(burgerOfTheDayRepository);
  }

  @Test
  void missingBurgerVisibilityCannotBeChanged() {
    User user = mock(User.class);
    when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));

    when(burgerOfTheDayRepository.findById(42L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.setBurgerOfTheDayVisibility(42L, "tester", true))
        .isInstanceOf(BurgerOfTheDayNotFoundException.class);
  }

  @Test
  void nonCreatorCannotSetBurgerVisibility() {
    User authenticatedUser = mock(User.class);
    User creator = mock(User.class);
    when(userRepository.findByUsername("tester")).thenReturn(Optional.of(authenticatedUser));

    when(authenticatedUser.getId())
        .thenReturn(UUID.fromString("241cda9e-95be-40f8-968b-5bfe8da19cc3"));
    when(creator.getId()).thenReturn(UUID.fromString("e162dda0-6fa8-4698-95c3-821160644a36"));

    BurgerOfTheDay burger = mock(BurgerOfTheDay.class);
    when(burger.getCreator()).thenReturn(creator);
    when(burgerOfTheDayRepository.findById(42L)).thenReturn(Optional.of(burger));

    assertThatThrownBy(() -> service.setBurgerOfTheDayVisibility(42L, "tester", true))
        .isInstanceOf(BurgerOfTheDayForbiddenException.class);
    verify(burger, never()).setHidden(anyBoolean());
  }

  @Test
  void publishedBurgersCanBeListedByCreator() {
    User creator = mock(User.class);
    when(creator.getUsername()).thenReturn("tester");
    Instant publishedAt = Instant.parse("2026-08-09T12:00:00Z");

    BurgerOfTheDay burger = mock(BurgerOfTheDay.class);
    when(burger.getCreator()).thenReturn(creator);
    when(burger.getId()).thenReturn(42L);
    when(burger.getText()).thenReturn("burger text");
    when(burger.getPublishedAt()).thenReturn(publishedAt);
    when(burger.getCommentary()).thenReturn("burger commentary");

    Pageable pageable =
        PageRequest.of(0, 50, Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id")));
    PageImpl<BurgerOfTheDay> repositoryPage = new PageImpl<>(List.of(burger), pageable, 1);

    when(burgerOfTheDayRepository.findByHiddenFalseAndCreatorUsername("tester", pageable))
        .thenReturn(repositoryPage);

    PublishedBurgerOfTheDayPageResponse response =
        service.getBurgersOfTheDay(Optional.empty(), Optional.of("tester"), 0, 50);

    PublishedBurgerOfTheDayResponse first = response.content().get(0);
    assertThat(first.id()).isEqualTo(42L);
    assertThat(first.text()).isEqualTo("burger text");
    assertThat(first.commentary()).isEqualTo("burger commentary");
    assertThat(first.publishedAt()).isEqualTo(publishedAt);
    assertThat(first.createdBy()).isEqualTo("tester");

    verify(burgerOfTheDayRepository, never()).findByHiddenFalse(any(Pageable.class));
    verify(burgerOfTheDayRepository).findByHiddenFalseAndCreatorUsername("tester", pageable);
  }
}
