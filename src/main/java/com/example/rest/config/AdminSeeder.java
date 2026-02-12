package com.example.rest.config;

import com.example.rest.user.User;
import com.example.rest.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeeder {
  @Bean
  public CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      String username = "heung";
      String email = "heung@ubisam.com";
      if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
        return;
      }

      User admin = new User(username, email, passwordEncoder.encode("heung"), "ROLE_ADMIN");
      userRepository.save(admin);
    };
  }
}
