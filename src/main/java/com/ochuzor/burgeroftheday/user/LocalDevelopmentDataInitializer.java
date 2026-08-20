package com.ochuzor.burgeroftheday.user;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalDevelopmentDataInitializer implements ApplicationRunner {
  private final UserRepository userRepository;

  public LocalDevelopmentDataInitializer(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (this.userRepository.findByUsername("tester").isEmpty()) {
      this.userRepository.save(new User("tester", "Tester, Local Dev"));
    }
  }
}
