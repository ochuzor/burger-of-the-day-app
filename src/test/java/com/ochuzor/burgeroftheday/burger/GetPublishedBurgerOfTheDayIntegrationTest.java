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
class GetPublishedBurgerOfTheDayIntegrationTest {
  private final BurgerOfTheDayRepository burgerOfTheDayRepository;
  private final UserRepository userRepository;
  private final EntityManager entityManager;

  private final MockMvc mockMvc;

  @Autowired
  GetPublishedBurgerOfTheDayIntegrationTest(
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
  void publishedBurgerCanBeRetrievedThroughHttp() throws Exception {
    User testUser = new User("integration-tester-2", "Integration User");
    User savedUser = this.userRepository.save(testUser);

    BurgerOfTheDay burgerOfTheDay =
        new BurgerOfTheDay(
            "Spicy Burger", "Comes with spices", Instant.parse("2026-08-10T12:00:00Z"), savedUser);

    BurgerOfTheDay savedBurgerOfTheDay = this.burgerOfTheDayRepository.save(burgerOfTheDay);
    long id = savedBurgerOfTheDay.getId();

    this.entityManager.flush();
    this.entityManager.clear();

    mockMvc
        .perform(get("/burger-of-the-day/" + id).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.text").value("Spicy Burger"))
        .andExpect(jsonPath("$.commentary").value("Comes with spices"))
        .andExpect(jsonPath("$.published_at").value("2026-08-10T12:00:00Z"))
        .andExpect(jsonPath("$.created_by").value("integration-tester-2"));
  }
}
