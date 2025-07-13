package dev.francode.ordersystem.config.initializer;

import dev.francode.ordersystem.entity.UserApp;
import dev.francode.ordersystem.entity.enums.ERole;
import dev.francode.ordersystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInicializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInicializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Crear usuario admin
        if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {
            UserApp adminUser = new UserApp();
            adminUser.setEmail("admin@gmail.com");
            adminUser.setPassword(passwordEncoder.encode("123456"));
            adminUser.setRol(ERole.ADMIN);

            userRepository.save(adminUser);
            System.out.println("Usuario admin creado exitosamente.");
        } else {
            System.out.println("El usuario admin ya existe.");
        }
    }
}
