package com.ochuzor.burgeroftheday.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "app_user")
public class User {
  @Id @GeneratedValue @UuidGenerator private UUID id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(name = "display_name", length = 100)
  private String displayName;

  protected User() {}

  public User(String username, String displayName) {
    this.username = username;
    this.displayName = displayName;
  }

  public UUID getId() {
    return this.id;
  }

  public String getUsername() {
    return this.username;
  }

  public String getDisplayName() {
    return this.displayName;
  }
}
