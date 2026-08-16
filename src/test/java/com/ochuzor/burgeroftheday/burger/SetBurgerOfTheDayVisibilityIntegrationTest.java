package com.ochuzor.burgeroftheday.burger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class SetBurgerOfTheDayVisibilityIntegrationTest {
  private final BurgerOfTheDayRepository burgerOfTheDayRepository;
  private final UserRepository userRepository;
  private final EntityManager entityManager;
  private final MockMvc mockMvc;

  @Autowired
  SetBurgerOfTheDayVisibilityIntegrationTest(
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
  void creatorCanHideAndUnhideBurgerThroughHttp() throws Exception {
    burgerOfTheDayRepository.deleteAll();
    userRepository.deleteAll();
    entityManager.flush();

    User user = this.userRepository.save(new User("visibility-tester", "Tester, M.D."));
    BurgerOfTheDay burger =
        burgerOfTheDayRepository.save(
            new BurgerOfTheDay(
                "Burger#1", "burger 1", Instant.parse("2026-08-10T10:00:00Z"), user));
    long id = burger.getId();

    this.entityManager.flush();
    this.entityManager.clear();

    mockMvc
        .perform(
            patch("/burger-of-the-day/{id}/visibility", id)
                .header("X-Username", "visibility-tester")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"hidden":true}
                    """))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));

    this.entityManager.flush();
    this.entityManager.clear();

    mockMvc
        .perform(get("/burger-of-the-day/{id}", id).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("burger of the day not found"));

    mockMvc
        .perform(
            patch("/burger-of-the-day/{id}/visibility", id)
                .header("X-Username", "visibility-tester")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"hidden":false}
                    """))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));

    this.entityManager.flush();
    this.entityManager.clear();

    mockMvc
        .perform(get("/burger-of-the-day/{id}", id).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.text").value("Burger#1"))
        .andExpect(jsonPath("$.commentary").value("burger 1"))
        .andExpect(jsonPath("$.created_by").value("visibility-tester"))
        .andExpect(jsonPath("$.published_at").value("2026-08-10T10:00:00Z"));
  }
}
