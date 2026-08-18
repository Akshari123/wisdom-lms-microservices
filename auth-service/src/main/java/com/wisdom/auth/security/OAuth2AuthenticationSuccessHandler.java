package com.wisdom.auth.security;

import com.wisdom.auth.model.User;
import com.wisdom.auth.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public OAuth2AuthenticationSuccessHandler(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        String googleId = oAuth2User.getAttribute("sub");

        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setPicture(picture);
            user.setProvider("GOOGLE");
            user.setRole("STUDENT"); // Default safe role
            user.setUsername(email); // Fallback for existing username logic
            user.setPassword(UUID.randomUUID().toString()); // Secure random placeholder
            userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getEmail());

        // Redirect to frontend with token
        String frontendUrl = "http://localhost:5173/?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, frontendUrl);
    }
}
