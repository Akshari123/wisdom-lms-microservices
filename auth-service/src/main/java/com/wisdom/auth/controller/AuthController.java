package com.wisdom.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wisdom.auth.model.User;
import com.wisdom.auth.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Test endpoint
    @GetMapping("/test")
    public String test() {
        return "Auth Service is running!";
    }

    // Register new user
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userRepository.save(user);
    }

    // Login
    @PostMapping("/login")
    public String login(@RequestBody User loginUser) {

        User user = userRepository.findByUsername(loginUser.getUsername());

        if (user == null) {
            return "Invalid username or password";
        }

        if (!user.getPassword().equals(loginUser.getPassword())) {
            return "Invalid username or password";
        }

        return "Login successful";
    }
}
