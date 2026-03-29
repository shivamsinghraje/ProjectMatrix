package com.projectmatrix.initializer;

import com.projectmatrix.entity.Role;
import com.projectmatrix.entity.User;
import com.projectmatrix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Checking if any users exist
        if (userRepository.count() == 0) {
            // Creating default admin user
            User admin = new User();
            admin.setEmail("admin@pm.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRole(Role.ADMIN);
            admin.setActive(true);

            userRepository.save(admin);
            log.info("Default admin user created - Email: admin@pm.com, Password: admin123");
        }
    }
}