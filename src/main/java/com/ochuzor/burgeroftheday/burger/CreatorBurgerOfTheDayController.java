package com.ochuzor.burgeroftheday.burger;

import com.ochuzor.burgeroftheday.user.MissingUsernameException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/burger-of-the-day")
public class CreatorBurgerOfTheDayController {
  private final BurgerOfTheDayService service;

  public CreatorBurgerOfTheDayController(BurgerOfTheDayService service) {
    this.service = service;
  }

  @GetMapping
  ResponseEntity<CreatorBurgerOfTheDayPageResponse> getOwnBurgersOfTheDay(
      @RequestParam(name = "publish_date", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate publishDate,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size,
      @RequestHeader(value = "X-Username", required = false) String username) {
    if (username == null || username.isBlank()) {
      throw new MissingUsernameException();
    }

    CreatorBurgerOfTheDayPageResponse response =
        service.getCreatorBurgersOfTheDay(username, Optional.ofNullable(publishDate), page, size);

    return ResponseEntity.ok(response);
  }
}
