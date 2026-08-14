package com.ochuzor.burgeroftheday.burger;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CreateBurgerOfTheDayRequestTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void validRequestHasNoConstraintViolations() {
    var request =
        new CreateBurgerOfTheDayRequest(
            "Pickled Burger\nComes with pickles", "A briny burger idea.");

    Set<ConstraintViolation<CreateBurgerOfTheDayRequest>> violations = validator.validate(request);

    assertThat(violations).isEmpty();
  }

  @Test
  void blankTextRequestIsRejected() {
    var request = new CreateBurgerOfTheDayRequest("   ", "A briny burger idea.");

    Set<ConstraintViolation<CreateBurgerOfTheDayRequest>> violations = validator.validate(request);

    assertThat(violations)
        .singleElement()
        .satisfies(
            violation -> {
              assertThat(violation.getPropertyPath().toString()).isEqualTo("text");
              assertThat(violation.getConstraintDescriptor().getAnnotation())
                  .isInstanceOf(NotBlank.class);
            });
  }

  @Test
  void textOf150CharactersIsAccepted() {
    String text = "a".repeat(150);

    var request = new CreateBurgerOfTheDayRequest(text, "A briny burger idea.");

    Set<ConstraintViolation<CreateBurgerOfTheDayRequest>> violations = validator.validate(request);

    assertThat(violations).isEmpty();
  }

  @Test
  void textOver150CharactersIsRejected() {
    String text = "a".repeat(151);

    var request = new CreateBurgerOfTheDayRequest(text, "A briny burger idea.");

    Set<ConstraintViolation<CreateBurgerOfTheDayRequest>> violations = validator.validate(request);

    assertThat(violations)
        .singleElement()
        .satisfies(
            violation -> {
              assertThat(violation.getPropertyPath().toString()).isEqualTo("text");
              assertThat(violation.getConstraintDescriptor().getAnnotation())
                  .isInstanceOf(Size.class);
            });
  }

  @Test
  void nullCommentaryRequestIsAccepted() {
    var request = new CreateBurgerOfTheDayRequest("Pickled Burger\nComes with pickles", null);

    Set<ConstraintViolation<CreateBurgerOfTheDayRequest>> violations = validator.validate(request);

    assertThat(violations).isEmpty();
  }

  @Test
  void commentaryOver500CharactersIsRejected() {
    String commentary = "a".repeat(501);

    var request = new CreateBurgerOfTheDayRequest("A valid burger", commentary);

    Set<ConstraintViolation<CreateBurgerOfTheDayRequest>> violations = validator.validate(request);

    assertThat(violations)
        .singleElement()
        .satisfies(
            violation -> {
              assertThat(violation.getPropertyPath().toString()).isEqualTo("commentary");
              assertThat(violation.getConstraintDescriptor().getAnnotation())
                  .isInstanceOf(Size.class);
            });
  }

  @Test
  void commentaryOf500CharactersIsAccepted() {
    String commentary = "a".repeat(500);

    var request = new CreateBurgerOfTheDayRequest("A valid burger", commentary);

    Set<ConstraintViolation<CreateBurgerOfTheDayRequest>> violations = validator.validate(request);

    assertThat(violations).isEmpty();
  }
}
