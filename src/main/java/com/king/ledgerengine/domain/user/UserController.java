package com.king.ledgerengine.domain.user;

import com.king.ledgerengine.domain.user.dto.CreateUserDto;
import com.king.ledgerengine.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User registration and lookup")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Register a new user")
    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody CreateUserDto payload) {
        User user = userService.create(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(summary = "Get a user by ID")
    @GetMapping("/{id}")
    public User getById(@PathVariable String id) {
        return userService.getById(id);
    }
}