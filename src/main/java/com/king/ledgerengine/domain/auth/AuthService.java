package com.king.ledgerengine.domain.auth;

import com.king.ledgerengine.domain.auth.dto.LoginDto;
import com.king.ledgerengine.domain.auth.dto.RegisterDto;
import com.king.ledgerengine.domain.user.UserRepository;
import com.king.ledgerengine.domain.user.UserService;
import com.king.ledgerengine.domain.user.entity.User;
import com.king.ledgerengine.shared.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public User register(RegisterDto payload) {
        return userService.create(
                payload.getFirstName(),
                payload.getLastName(),
                payload.getEmail(),
                payload.getPassword()
        );
    }

    public Map<String, Object> login(LoginDto payload) {
        User user = userRepository.findByEmail(payload.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email"));

        if (!passwordEncoder.matches(payload.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return Map.of(
                "user", user,
                "token", token
        );
    }
}