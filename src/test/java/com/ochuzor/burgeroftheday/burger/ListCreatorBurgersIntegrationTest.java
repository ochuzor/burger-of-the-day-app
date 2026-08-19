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
public class ListCreatorBurgersIntegrationTest {
  private final BurgerOfTheDayRepository burgerOfTheDayRepository;
  private final UserRepository userRepository;
  private final EntityManager entityManager;
  private final MockMvc mockMvc;

  @Autowired
  ListCreatorBurgersIntegrationTest(
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
  void creatorCanListVisibleAndHiddenBurgersThroughHttp() throws Exception {
    burgerOfTheDayRepository.deleteAll();
    userRepository.deleteAll();
    entityManager.flush();

    User alice = this.userRepository.save(new User("alice", "User, Alice"));
    User bob = this.userRepository.save(new User("bob", "User, bob"));

    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Alice Burger#1", "alice burger 1", Instant.parse("2026-08-14T10:10:00Z"), alice));
    BurgerOfTheDay hiddenBurger =
        burgerOfTheDayRepository.save(
            new BurgerOfTheDay(
                "[HIDDEN] Alice Burger#2",
                "alice burger 2",
                Instant.parse("2026-08-14T10:12:00Z"),
                alice));
    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Alice Burger#3", "alice burger 2", Instant.parse("2026-08-13T10:10:00Z"), alice));
    burgerOfTheDayRepository.save(
        new BurgerOfTheDay(
            "Bob Burger", "Bob's burger", Instant.parse("2026-08-14T10:00:00Z"), bob));

    entityManager.flush();

    entityManager
        .createQuery(
            "update BurgerOfTheDay burger " + "set burger.hidden = true " + "where burger.id = :id")
        .setParameter("id", hiddenBurger.getId())
        .executeUpdate();

    entityManager.clear();

    mockMvc
        .perform(
            get("/me/burger-of-the-day")
                .header("X-Username", "alice")
                .param("publish_date", "2026-08-14")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content[0].text").value("[HIDDEN] Alice Burger#2"))
        .andExpect(jsonPath("$.content[0].published_at").value("2026-08-14T10:12:00Z"))
        .andExpect(jsonPath("$.content[0].created_by").value("alice"))
        .andExpect(jsonPath("$.content[0].hidden").value(true))
        .andExpect(jsonPath("$.content[1].text").value("Alice Burger#1"))
        .andExpect(jsonPath("$.content[1].published_at").value("2026-08-14T10:10:00Z"))
        .andExpect(jsonPath("$.content[1].created_by").value("alice"))
        .andExpect(jsonPath("$.content[1].hidden").value(false))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(50))
        .andExpect(jsonPath("$.total_elements").value(2))
        .andExpect(jsonPath("$.total_pages").value(1))
        .andExpect(jsonPath("$.content[2]").doesNotExist());
  }
}
