package com.elowen.identity.config;

import com.elowen.identity.entity.Client;
import com.elowen.identity.entity.User;
import com.elowen.identity.repository.ClientRepository;
import com.elowen.identity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- DATA INITIALIZER STARTED ---");
        
        String clientName = "Test Client";
        Optional<Client> clientOpt = clientRepository.findByName(clientName);
        
        Client client;
        if (clientOpt.isEmpty()) {
            System.out.println("Creating Test Client...");
            client = new Client();
            client.setName(clientName);
            client.setClientDomain("test-client");
            client.setEnabled(true);
            client.setNumberOfUsersAllowed(100);
            clientRepository.save(client);
        } else {
            client = clientOpt.get();
            System.out.println("Test Client found: " + client.getId());
        }

        String username = "admin";
        Optional<User> userOpt = userRepository.findByUsernameOrEmailAndClientId(username, client.getId());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("Resetting admin password...");
            user.setPassword(passwordEncoder.encode("password"));
            userRepository.save(user); // Password updated to valid hash
            System.out.println("Admin password reset to 'password'");
        } else {
             System.out.println("Creating admin user...");
             User user = new User();
             user.setClientId(client.getId());
             user.setUsername(username);
             user.setEmailId("admin@example.com");
             user.setName("Admin User");
             user.setRole(User.Role.ADMIN);
             user.setEnabled(true);
             user.setPassword(passwordEncoder.encode("password"));
             userRepository.save(user);
             System.out.println("Admin user created with password 'password'");
        }
        System.out.println("--- DATA INITIALIZER FINISHED ---");
    }
}
