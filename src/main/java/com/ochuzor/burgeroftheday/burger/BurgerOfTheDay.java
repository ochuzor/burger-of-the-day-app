package com.ochuzor.burgeroftheday.burger;

import com.ochuzor.burgeroftheday.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "burger_of_the_day")
public class BurgerOfTheDay {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "text", length = 150, nullable = false)
  private String text;

  @Column(name = "commentary", length = 500)
  private String commentary;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "publish_date", nullable = false)
  private LocalDate publishDate;

  @Column(name = "hidden", nullable = false)
  private boolean hidden;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "creator_id", nullable = false)
  private User creator;

  protected BurgerOfTheDay() {}

  public BurgerOfTheDay(
      String text, String commentary, Instant createdAt, LocalDate publishDate, User creator) {
    this.text = text;
    this.commentary = commentary;
    this.createdAt = createdAt;
    this.publishDate = publishDate;
    this.creator = creator;

    this.hidden = false;
  }

  public Long getId() {
    return this.id;
  }

  public String getText() {
    return this.text;
  }

  public String getCommentary() {
    return this.commentary;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public LocalDate getPublishDate() {
    return this.publishDate;
  }

  public User getCreator() {
    return this.creator;
  }

  public boolean isHidden() {
    return this.hidden;
  }
}
