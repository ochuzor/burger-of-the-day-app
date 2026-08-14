package com.ochuzor.burgeroftheday.burger;

import com.ochuzor.burgeroftheday.user.MissingUsernameException;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/burger-of-the-day")
public class BurgerOfTheDayController {

  private final BurgerOfTheDayService service;

  public BurgerOfTheDayController(BurgerOfTheDayService service) {
    this.service = service;
  }

  @PostMapping
  ResponseEntity<Void> createBurgerOfTheDay(
      @RequestHeader(value = "X-Username", required = false) String username,
      @Valid @RequestBody CreateBurgerOfTheDayRequest request) {
    if (username == null || username.isBlank()) {
      throw new MissingUsernameException();
    }

    Long id = this.service.createBurgerOfTheDay(request.text(), request.commentary(), username);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();

    return ResponseEntity.created(location).build();
  }

  @GetMapping("/{id}")
  ResponseEntity<PublishedBurgerOfTheDayResponse> getPublishedBurgerOfTheDay(
      @PathVariable long id) {
    PublishedBurgerOfTheDayResponse burger = this.service.getPublishedBurgerOfTheDay(id);

    return ResponseEntity.ok(burger);
  }
}
