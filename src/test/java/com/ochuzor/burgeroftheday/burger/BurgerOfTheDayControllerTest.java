package com.ochuzor.burgeroftheday.burger;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ochuzor.burgeroftheday.user.UnknownUserException;
import java.time.LocalDate;
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
    when(service.createBurgerOfTheDay(
            "Spicy Burger", "Comes with spices", LocalDate.of(2026, 8, 10), "tester"))
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
                      "commentary": "Comes with spices",
                      "publish_date": "2026-08-10"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/burger-of-the-day/42"))
        .andExpect(content().string(""));

    verify(service)
        .createBurgerOfTheDay(
            "Spicy Burger", "Comes with spices", LocalDate.of(2026, 8, 10), "tester");
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
                      "commentary": "Comes with spices",
                      "publish_date": "2026-08-10"
                    }
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("unauthorized"));

    verifyNoInteractions(service);
  }

  @Test
  void unknownUsernameReturnsUnauthorized() throws Exception {
    when(service.createBurgerOfTheDay(
            "Spicy Burger", "Comes with spices", LocalDate.of(2026, 8, 10), "unknown"))
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
                      "commentary": "Comes with spices",
                      "publish_date": "2026-08-10"
                    }
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("unauthorized"));

    verify(service)
        .createBurgerOfTheDay(
            "Spicy Burger", "Comes with spices", LocalDate.of(2026, 8, 10), "unknown");
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
                      "commentary": "Comes with spices",
                      "publish_date": "2026-08-10"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("text should not be empty"));

    verifyNoInteractions(service);
  }

  @Test
  void pastPublicationDateReturnsBadRequest() throws Exception {
    when(service.createBurgerOfTheDay(
            "Spicy Burger", "Comes with spices", LocalDate.of(2026, 8, 9), "tester"))
        .thenThrow(new PastPublicationDateException("Date: 2026-08-09 is in the past"));

    mockMvc
        .perform(
            post("/burger-of-the-day")
                .header("X-Username", "tester")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "text": "Spicy Burger",
                      "commentary": "Comes with spices",
                      "publish_date": "2026-08-09"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Date: 2026-08-09 is in the past"));

    verify(service)
        .createBurgerOfTheDay(
            "Spicy Burger", "Comes with spices", LocalDate.of(2026, 8, 9), "tester");
  }

  @Test
  void invalidPublishDateReturnsBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/burger-of-the-day")
                .header("X-Username", "tester")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "text": "Spicy Burger",
                      "commentary": "Comes with spices",
                      "publish_date": "2026-02-30"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("malformed request"));

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
                      "commentary": "Comes with spices",
                      "publish_date": "2026-02-30"

                    """))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("malformed request"));

    verifyNoInteractions(service);
  }
}
