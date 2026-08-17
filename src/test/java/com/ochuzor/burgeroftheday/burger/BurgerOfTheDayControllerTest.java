package com.ochuzor.burgeroftheday.burger;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ochuzor.burgeroftheday.user.UnknownUserException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

  @Test
  void publishedBurgersCanBeListedWithDefaults() throws Exception {
    PublishedBurgerOfTheDayResponse burger =
        new PublishedBurgerOfTheDayResponse(
            42L,
            "Fancy Burger",
            "Comes with integration tests",
            Instant.parse("2026-08-14T12:00:00Z"),
            "tester");

    when(service.getBurgersOfTheDay(Optional.empty(), Optional.empty(), 0, 50))
        .thenReturn(new PublishedBurgerOfTheDayPageResponse(List.of(burger), 0, 50, 1, 1));

    mockMvc
        .perform(get("/burger-of-the-day").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content[0].id").value(42))
        .andExpect(jsonPath("$.content[0].text").value("Fancy Burger"))
        .andExpect(jsonPath("$.content[0].commentary").value("Comes with integration tests"))
        .andExpect(jsonPath("$.content[0].published_at").value("2026-08-14T12:00:00Z"))
        .andExpect(jsonPath("$.content[0].created_by").value("tester"))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(50))
        .andExpect(jsonPath("$.total_elements").value(1))
        .andExpect(jsonPath("$.total_pages").value(1));

    verify(service).getBurgersOfTheDay(Optional.empty(), Optional.empty(), 0, 50);
  }

  @Test
  void publishedBurgersCanBeFilteredByDateAndPagination() throws Exception {
    PublishedBurgerOfTheDayResponse burger =
        new PublishedBurgerOfTheDayResponse(
            42L,
            "Fancy Burger",
            "Comes with integration tests",
            Instant.parse("2026-08-14T12:00:00Z"),
            "tester");

    LocalDate requestedDate = LocalDate.of(2026, 8, 14);
    when(service.getBurgersOfTheDay(Optional.of(requestedDate), Optional.empty(), 2, 25))
        .thenReturn(new PublishedBurgerOfTheDayPageResponse(List.of(burger), 2, 25, 51, 3));

    mockMvc
        .perform(
            get("/burger-of-the-day")
                .param("publish_date", "2026-08-14")
                .param("page", "2")
                .param("size", "25")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content[0].id").value(42))
        .andExpect(jsonPath("$.content[0].text").value("Fancy Burger"))
        .andExpect(jsonPath("$.content[0].commentary").value("Comes with integration tests"))
        .andExpect(jsonPath("$.content[0].published_at").value("2026-08-14T12:00:00Z"))
        .andExpect(jsonPath("$.content[0].created_by").value("tester"))
        .andExpect(jsonPath("$.page").value(2))
        .andExpect(jsonPath("$.size").value(25))
        .andExpect(jsonPath("$.total_elements").value(51))
        .andExpect(jsonPath("$.total_pages").value(3));
  }

  @Test
  void invalidPublishDateReturnsBadRequest() throws Exception {
    mockMvc
        .perform(
            get("/burger-of-the-day")
                .param("publish_date", "2026-02-30")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("invalid publish date"));

    verifyNoInteractions(service);
  }

  @Test
  void negativePageReturnsBadRequest() throws Exception {
    mockMvc
        .perform(get("/burger-of-the-day").param("page", "-1").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("invalid pagination"));

    verifyNoInteractions(service);
  }

  @Test
  void zeroPageSizeReturnsBadRequest() throws Exception {
    mockMvc
        .perform(get("/burger-of-the-day").param("size", "0").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("invalid pagination"));

    verifyNoInteractions(service);
  }

  @Test
  void pageSizeAboveMaximumReturnsBadRequest() throws Exception {
    mockMvc
        .perform(get("/burger-of-the-day").param("size", "201").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("invalid pagination"));

    verifyNoInteractions(service);
  }

  @Test
  void nonNumericPageReturnsBadRequest() throws Exception {
    mockMvc
        .perform(
            get("/burger-of-the-day").param("page", "first").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("invalid pagination"));

    verifyNoInteractions(service);
  }

  @Test
  void creatorCanHideBurger() throws Exception {
    mockMvc
        .perform(
            patch("/burger-of-the-day/42/visibility")
                .header("X-Username", "tester")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"hidden":true}
                    """))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));

    verify(service).setBurgerOfTheDayVisibility(42L, "tester", true);
  }

  @Test
  void missingUsernameCannotSetBurgerVisibility() throws Exception {
    mockMvc
        .perform(
            patch("/burger-of-the-day/42/visibility")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"hidden":true}
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("unauthorized"));

    verifyNoInteractions(service);
  }

  @Test
  void missingHiddenReturnsBadRequest() throws Exception {
    mockMvc
        .perform(
            patch("/burger-of-the-day/42/visibility")
                .header("X-Username", "tester")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("hidden is required"));

    verifyNoInteractions(service);
  }

  @Test
  void nonCreatorCannotSetBurgerVisibility() throws Exception {
    doThrow(new BurgerOfTheDayForbiddenException())
        .when(service)
        .setBurgerOfTheDayVisibility(42L, "intruder", true);

    mockMvc
        .perform(
            patch("/burger-of-the-day/42/visibility")
                .header("X-Username", "intruder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"hidden":true}
                    """))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("forbidden"));
  }

  @Test
  void publishedBurgersCanBeFilteredByCreator() throws Exception {
    PublishedBurgerOfTheDayResponse burger =
        new PublishedBurgerOfTheDayResponse(
            42L,
            "Fancy Burger",
            "Comes with integration tests",
            Instant.parse("2026-08-14T12:00:00Z"),
            "tester");

    when(service.getBurgersOfTheDay(Optional.empty(), Optional.of("tester"), 0, 50))
        .thenReturn(new PublishedBurgerOfTheDayPageResponse(List.of(burger), 0, 50, 1, 1));

    mockMvc
        .perform(
            get("/burger-of-the-day")
                .param("created_by", "tester")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content[0].id").value(42))
        .andExpect(jsonPath("$.content[0].text").value("Fancy Burger"))
        .andExpect(jsonPath("$.content[0].commentary").value("Comes with integration tests"))
        .andExpect(jsonPath("$.content[0].published_at").value("2026-08-14T12:00:00Z"))
        .andExpect(jsonPath("$.content[0].created_by").value("tester"))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(50))
        .andExpect(jsonPath("$.total_elements").value(1))
        .andExpect(jsonPath("$.total_pages").value(1));

    verify(service).getBurgersOfTheDay(Optional.empty(), Optional.of("tester"), 0, 50);
  }

  @Test
  void publishedBurgersCanBeFilteredByCreatorAndDate() throws Exception {
    PublishedBurgerOfTheDayResponse burger =
        new PublishedBurgerOfTheDayResponse(
            42L,
            "Fancy Burger",
            "Comes with integration tests",
            Instant.parse("2026-08-14T12:00:00Z"),
            "tester");

    when(service.getBurgersOfTheDay(
            Optional.of(LocalDate.of(2026, 8, 14)), Optional.of("tester"), 0, 50))
        .thenReturn(new PublishedBurgerOfTheDayPageResponse(List.of(burger), 0, 50, 1, 1));

    mockMvc
        .perform(
            get("/burger-of-the-day")
                .param("publish_date", "2026-08-14")
                .param("created_by", "tester")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content[0].id").value(42))
        .andExpect(jsonPath("$.content[0].text").value("Fancy Burger"))
        .andExpect(jsonPath("$.content[0].commentary").value("Comes with integration tests"))
        .andExpect(jsonPath("$.content[0].published_at").value("2026-08-14T12:00:00Z"))
        .andExpect(jsonPath("$.content[0].created_by").value("tester"))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(50))
        .andExpect(jsonPath("$.total_elements").value(1))
        .andExpect(jsonPath("$.total_pages").value(1));

    verify(service)
        .getBurgersOfTheDay(Optional.of(LocalDate.of(2026, 8, 14)), Optional.of("tester"), 0, 50);
  }

  @Test
  void blankCreatorReturnsBadRequest() throws Exception {
    mockMvc
        .perform(
            get("/burger-of-the-day").param("created_by", "   ").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("created_by must not be blank"));

    verifyNoInteractions(service);
  }
}
