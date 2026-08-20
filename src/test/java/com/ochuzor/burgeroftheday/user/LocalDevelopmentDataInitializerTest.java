package com.ochuzor.burgeroftheday.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocalDevelopmentDataInitializerTest {
  @Mock private UserRepository userRepository;
  private LocalDevelopmentDataInitializer initializer;

  @BeforeEach
  void setUp() {
    initializer = new LocalDevelopmentDataInitializer(userRepository);
  }

  @Test
  void missingSeedUserIsCreated() {
    when(userRepository.findByUsername("tester")).thenReturn(Optional.empty());

    initializer.run(null);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());

    User savedUser = userCaptor.getValue();
    assertEquals("tester", savedUser.getUsername());
    assertEquals("Tester, Local Dev", savedUser.getDisplayName());
  }

  @Test
  void existingSeedUserIsNotCreated() {
    User existingUser = new User("tester", "Existing Tester");
    when(userRepository.findByUsername("tester")).thenReturn(Optional.of(existingUser));

    initializer.run(null);

    verify(userRepository, never()).save(any(User.class));
  }
}
