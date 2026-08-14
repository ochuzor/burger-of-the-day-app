package com.ochuzor.burgeroftheday.burger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ochuzor.burgeroftheday.user.User;
import com.ochuzor.burgeroftheday.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CreateBurgerOfTheDayIntegrationTest {
  private final BurgerOfTheDayRepository burgerOfTheDayRepository;
  private final UserRepository userRepository;
  private final EntityManager entityManager;

  private final MockMvc mockMvc;

  @Autowired
  CreateBurgerOfTheDayIntegrationTest(
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
  void createRequestPersistsBurgerAndReturnsItsLocation() throws Exception {
    User testUser = new User("integration-tester", "Integration User");
    this.userRepository.save(testUser);

    long burgerCountBeforeRequest = this.burgerOfTheDayRepository.count();
    Instant beforeRequest = Instant.now();

    this.mockMvc
        .perform(
            post("/burger-of-the-day")
                .header("X-Username", "integration-tester")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "text": "Integration Burger",
                      "commentary": "Comes with integration tests"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(header().exists(HttpHeaders.LOCATION))
        .andExpect(content().string(""));

    Instant afterRequest = Instant.now();
    this.entityManager.flush();
    this.entityManager.clear();

    assertThat(this.burgerOfTheDayRepository.count()).isEqualTo(burgerCountBeforeRequest + 1);

    BurgerOfTheDay foundBurgerOfTheDay =
        this.burgerOfTheDayRepository.findAll().stream()
            .filter(burger -> burger.getText().equals("Integration Burger"))
            .findFirst()
            .orElseThrow();

    assertThat(foundBurgerOfTheDay.getText()).isEqualTo("Integration Burger");
    assertThat(foundBurgerOfTheDay.getCommentary()).isEqualTo("Comes with integration tests");
    assertThat(foundBurgerOfTheDay.getPublishedAt())
        .isAfterOrEqualTo(beforeRequest)
        .isBeforeOrEqualTo(afterRequest);
    assertThat(foundBurgerOfTheDay.isHidden()).isFalse();
    assertThat(foundBurgerOfTheDay.getCreator().getUsername()).isEqualTo("integration-tester");
  }
}
