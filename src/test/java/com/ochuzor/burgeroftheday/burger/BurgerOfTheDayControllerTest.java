package com.ochuzor.burgeroftheday.burger;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ochuzor.burgeroftheday.user.UnknownUserException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BurgerOfTheDayController.class)
class BurgerOfTheDayControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private BurgerOfTheDayService service;

  @Test
  void validRequestReturnsCreatedLocation() throws Exception {
    when(service.createBurgerOfTheDay("Spicy Burger", "Comes with spices", "tester"))
        .thenReturn(42L);

    mockMvc
        .perform(
            post("/burger-of-the-day")
                .header("X-Username", "tester")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "text": "Spicy Burger",
                      "commentary": "Comes with spices"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/burger-of-the-day/42"))
        .andExpect(content().string(""));

    verify(service).createBurgerOfTheDay("Spicy Burger", "Comes with spices", "tester");
  }

  @Test
  void missingUsernameHeaderReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/burger-of-the-day")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "text": "Spicy Burger",
                      "commentary": "Comes with spices"
                    }
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("unauthorized"));

    verifyNoInteractions(service);
  }

  @Test
  void unknownUsernameReturnsUnauthorized() throws Exception {
    when(service.createBurgerOfTheDay("Spicy Burger", "Comes with spices", "unknown"))
        .thenThrow(new UnknownUserException("user not found"));

    mockMvc
        .perform(
            post("/burger-of-the-day")
                .header("X-Username", "unknown")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "text": "Spicy Burger",
                      "commentary": "Comes with spices"
                    }
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("unauthorized"));

    verify(service).createBurgerOfTheDay("Spicy Burger", "Comes with spices", "unknown");
  }

  @Test
  void blankTextReturnsBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/burger-of-the-day")
                .header("X-Username", "tester")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "text": "     ",
                      "commentary": "Comes with spices"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("text should not be empty"));

    verifyNoInteractions(service);
  }

  @Test
  void malformedJsonReturnsBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/burger-of-the-day")
                .header("X-Username", "tester")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "text": "Spicy Burger",
                      "commentary": "Comes with spices"

                    """))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("malformed request"));

    verifyNoInteractions(service);
  }

  @Test
  void publishedBurgerCanBeRetrievedById() throws Exception {
    when(service.getPublishedBurgerOfTheDay(42L))
        .thenReturn(
            new PublishedBurgerOfTheDayResponse(
                42L,
                "Fancy Burger",
                "Comes with unit tests",
                Instant.parse("2026-08-10T12:00:00Z"),
                "tester"));

    mockMvc
        .perform(get("/burger-of-the-day/42").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(42))
        .andExpect(jsonPath("$.text").value("Fancy Burger"))
        .andExpect(jsonPath("$.commentary").value("Comes with unit tests"))
        .andExpect(jsonPath("$.created_by").value("tester"))
        .andExpect(jsonPath("$.published_at").value("2026-08-10T12:00:00Z"));

    verify(service).getPublishedBurgerOfTheDay(42L);
  }

  @Test
  void nonPublicBurgerReturnsNotFound() throws Exception {
    when(service.getPublishedBurgerOfTheDay(42L)).thenThrow(new BurgerOfTheDayNotFoundException());

    mockMvc
        .perform(get("/burger-of-the-day/42").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("burger of the day not found"));

    verify(service).getPublishedBurgerOfTheDay(42L);
  }
}
