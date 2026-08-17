package com.ochuzor.burgeroftheday.burger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ochuzor.burgeroftheday.user.User;
import com.ochuzor.burgeroftheday.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ListPublishedBurgersIntegrationTest {
  private final BurgerOfTheDayRepository burgerOfTheDayRepository;
  private final UserRepository userRepository;
  private final EntityManager entityManager;
  private final MockMvc mockMvc;

  @Autowired
  ListPublishedBurgersIntegrationTest(
      BurgerOfTheDayRepository burgerOfTheDayRepository,
      UserRepository userRepository,
      EntityManager entityManager,
      MockMvc mockMvc) {
    this.burgerOfTheDayRepository = burgerOfTheDayRepository;
    this.userRepository = userRepository;
    this.entityManager = entityManager;
    this.mockMvc = mockMvc;
  }

  @Test
  void visibleBurgersAreListedNewestFirstThroughHttp() throws Exception {
    burgerOfTheDayRepository.deleteAll();
    userRepository.deleteAll();
    entityManager.flush();

    User user = this.userRepository.save(new User("tester", "Tester, M.D."));

    burgerOfTheDayRepository.save(
        new BurgerOfTheDay("Burger#1", "burger 1", Instant.parse("2026-08-10T10:00:00Z"), user));
    burgerOfTheDayRepository.save(
        new BurgerOfTheDay("Burger#2", "burger 2", Instant.parse("2026-08-10T12:00:00Z"), user));
    BurgerOfTheDay hiddenBurger =
        burgerOfTheDayRepository.save(
            new BurgerOfTheDay(
                "Burger#3", "burger 3", Instant.parse("2026-08-10T13:00:00Z"), user));

    entityManager.flush();

    entityManager
        .createQuery(
            "update BurgerOfTheDay burger " + "set burger.hidden = true " + "where burger.id = :id")
        .setParameter("id", hiddenBurger.getId())
        .executeUpdate();

    entityManager.clear();

    mockMvc
        .perform(get("/burger-of-the-day").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content[0].text").value("Burger#2"))
        .andExpect(jsonPath("$.content[0].published_at").value("2026-08-10T12:00:00Z"))
        .andExpect(jsonPath("$.content[0].created_by").value("tester"))
        .andExpect(jsonPath("$.content[1].text").value("Burger#1"))
        .andExpect(jsonPath("$.content[1].published_at").value("2026-08-10T10:00:00Z"))
        .andExpect(jsonPath("$.content[1].created_by").value("tester"))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(50))
        .andExpect(jsonPath("$.total_elements").value(2))
        .andExpect(jsonPath("$.total_pages").value(1))
        .andExpect(jsonPath("$.content[2]").doesNotExist());
  }

  @Test
  void publishedBurgersCanBeFilteredByCreatorThroughHttp() throws Exception {
    burgerOfTheDayRepository.deleteAll();
    userRepository.deleteAll();
    entityManager.flush();

    User requestedUser = this.userRepository.save(new User("requested-user", "Requested user"));
    User creatorUser = this.userRepository.save(new User("creator-user", "Creator user"));

    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Burger#1", "burger 1", Instant.parse("2026-08-10T10:00:00Z"), requestedUser));
    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Burger#2", "burger 2", Instant.parse("2026-08-10T12:00:00Z"), creatorUser));
    BurgerOfTheDay hiddenBurger =
        burgerOfTheDayRepository.save(
            new BurgerOfTheDay(
                "Burger#3", "burger 3", Instant.parse("2026-08-10T13:00:00Z"), requestedUser));

    entityManager.flush();

    entityManager
        .createQuery(
            "update BurgerOfTheDay burger " + "set burger.hidden = true " + "where burger.id = :id")
        .setParameter("id", hiddenBurger.getId())
        .executeUpdate();

    entityManager.clear();

    mockMvc
        .perform(
            get("/burger-of-the-day")
                .param("created_by", requestedUser.getUsername())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total_elements").value(1))
        .andExpect(jsonPath("$.content[0].created_by").value("requested-user"))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content[0].text").value("Burger#1"))
        .andExpect(jsonPath("$.content[1]").doesNotExist());
  }
}
