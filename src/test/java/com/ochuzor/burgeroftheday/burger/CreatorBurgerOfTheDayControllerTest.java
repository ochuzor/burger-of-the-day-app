package com.ochuzor.burgeroftheday.burger;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CreatorBurgerOfTheDayController.class)
public class CreatorBurgerOfTheDayControllerTest {
  @Autowired private MockMvc mockMvc;
  @MockitoBean private BurgerOfTheDayService service;

  @Test
  void creatorCanListOwnBurgersIncludingHiddenPosts() throws Exception {
    List<CreatorBurgerOfTheDayResponse> burgers =
        List.of(
            new CreatorBurgerOfTheDayResponse(
                20L,
                "Burger #1",
                "burger #1 commentary",
                Instant.parse("2026-08-14T12:00:00Z"),
                "alice",
                false),
            new CreatorBurgerOfTheDayResponse(
                21L,
                "Burger #2",
                "burger #2 commentary",
                Instant.parse("2026-08-14T12:00:00Z"),
                "alice",
                true));

    when(service.getCreatorBurgersOfTheDay("alice", Optional.empty(), 0, 50))
        .thenReturn(new CreatorBurgerOfTheDayPageResponse(burgers, 0, 50, 2, 1));

    mockMvc
        .perform(
            get("/me/burger-of-the-day")
                .header("X-Username", "alice")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content[0].id").value(20))
        .andExpect(jsonPath("$.content[0].text").value("Burger #1"))
        .andExpect(jsonPath("$.content[0].commentary").value("burger #1 commentary"))
        .andExpect(jsonPath("$.content[0].published_at").value("2026-08-14T12:00:00Z"))
        .andExpect(jsonPath("$.content[0].created_by").value("alice"))
        .andExpect(jsonPath("$.content[0].hidden").value(false))
        .andExpect(jsonPath("$.content[1].id").value(21))
        .andExpect(jsonPath("$.content[1].text").value("Burger #2"))
        .andExpect(jsonPath("$.content[1].commentary").value("burger #2 commentary"))
        .andExpect(jsonPath("$.content[1].published_at").value("2026-08-14T12:00:00Z"))
        .andExpect(jsonPath("$.content[1].created_by").value("alice"))
        .andExpect(jsonPath("$.content[1].hidden").value(true))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(50))
        .andExpect(jsonPath("$.total_elements").value(2))
        .andExpect(jsonPath("$.total_pages").value(1));

    verify(service).getCreatorBurgersOfTheDay("alice", Optional.empty(), 0, 50);
  }

  @Test
  void creatorCanFilterOwnBurgersByPublicationDateAndPagination() throws Exception {
    List<CreatorBurgerOfTheDayResponse> burgers =
        List.of(
            new CreatorBurgerOfTheDayResponse(
                20L,
                "Burger #1",
                "burger #1 commentary",
                Instant.parse("2026-08-14T12:00:00Z"),
                "alice",
                false),
            new CreatorBurgerOfTheDayResponse(
                21L,
                "Burger #2",
                "burger #2 commentary",
                Instant.parse("2026-08-14T12:00:00Z"),
                "alice",
                true));

    LocalDate requestedDate = LocalDate.of(2026, 8, 14);
    when(service.getCreatorBurgersOfTheDay("alice", Optional.of(requestedDate), 2, 10))
        .thenReturn(new CreatorBurgerOfTheDayPageResponse(burgers, 2, 10, 2, 1));

    mockMvc
        .perform(
            get("/me/burger-of-the-day")
                .header("X-Username", "alice")
                .param("publish_date", "2026-08-14")
                .param("page", "2")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content[0].id").value(20))
        .andExpect(jsonPath("$.content[0].text").value("Burger #1"))
        .andExpect(jsonPath("$.content[0].commentary").value("burger #1 commentary"))
        .andExpect(jsonPath("$.content[0].published_at").value("2026-08-14T12:00:00Z"))
        .andExpect(jsonPath("$.content[0].created_by").value("alice"))
        .andExpect(jsonPath("$.content[0].hidden").value(false))
        .andExpect(jsonPath("$.content[1].id").value(21))
        .andExpect(jsonPath("$.content[1].text").value("Burger #2"))
        .andExpect(jsonPath("$.content[1].commentary").value("burger #2 commentary"))
        .andExpect(jsonPath("$.content[1].published_at").value("2026-08-14T12:00:00Z"))
        .andExpect(jsonPath("$.content[1].created_by").value("alice"))
        .andExpect(jsonPath("$.content[1].hidden").value(true))
        .andExpect(jsonPath("$.page").value(2))
        .andExpect(jsonPath("$.size").value(10))
        .andExpect(jsonPath("$.total_elements").value(2))
        .andExpect(jsonPath("$.total_pages").value(1));

    verify(service).getCreatorBurgersOfTheDay("alice", Optional.of(requestedDate), 2, 10);
  }

  @Test
  void missingUsernameCannotListCreatorBurgers() throws Exception {
    mockMvc
        .perform(
            get("/me/burger-of-the-day")
                .param("publish_date", "2026-08-14")
                .param("page", "2")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("unauthorized"));

    verifyNoInteractions(service);
  }

  @Test
  void blankUsernameCannotListCreatorBurgers() throws Exception {
    mockMvc
        .perform(
            get("/me/burger-of-the-day")
                .header("X-Username", "   ")
                .param("publish_date", "2026-08-14")
                .param("page", "2")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("unauthorized"));

    verifyNoInteractions(service);
  }

  @Test
  void unknownUsernameCannotListCreatorBurgers() throws Exception {
    when(service.getCreatorBurgersOfTheDay(
            "unknown", Optional.of(LocalDate.of(2026, 8, 14)), 2, 10))
        .thenThrow(new UnknownUserException("user not found"));

    mockMvc
        .perform(
            get("/me/burger-of-the-day")
                .header("X-Username", "unknown")
                .param("publish_date", "2026-08-14")
                .param("page", "2")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("unauthorized"));
  }

  @Test
  void invalidPublicationDateCannotListCreatorBurgers() throws Exception {
    mockMvc
        .perform(
            get("/me/burger-of-the-day")
                .header("X-Username", "alice")
                .param("publish_date", "2026-02-30")
                .param("page", "2")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("invalid publish date"));

    verifyNoInteractions(service);
  }

  @Test
  void invalidPaginationCannotListCreatorBurgers() throws Exception {
    mockMvc
        .perform(
            get("/me/burger-of-the-day")
                .header("X-Username", "alice")
                .param("publish_date", "2026-08-14")
                .param("page", "-1")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("invalid pagination"));

    verifyNoInteractions(service);
  }
}
