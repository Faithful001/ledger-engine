package com.king.ledgerengine.domain.user;

import com.king.ledgerengine.domain.user.dto.CreateUserDto;
import com.king.ledgerengine.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User create(CreateUserDto payload) {
        if (userRepository.existsByEmail(payload.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = User.builder()
                .email(payload.getEmail())
                .password(passwordEncoder.encode(payload.getPassword()))
                .build();

        return userRepository.save(user);
    }

    public User getById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}