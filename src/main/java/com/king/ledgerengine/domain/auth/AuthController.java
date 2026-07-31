package com.king.ledgerengine.domain.auth;

import com.king.ledgerengine.domain.auth.dto.LoginDto;
import com.king.ledgerengine.domain.auth.dto.RegisterDto;
import com.king.ledgerengine.domain.user.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "User authentication")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterDto payload) {
        User user = authService.register(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginDto payload) {
        Map<String, Object> result = authService.login(payload);
        return ResponseEntity.ok(result);
    }
}