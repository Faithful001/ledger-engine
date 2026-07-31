package com.king.ledgerengine.domain.user;

import com.king.ledgerengine.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User registration and lookup")
public class UserController {
    private final UserService userService;

//    @Operation(summary = "Get a user by ID")
//    @GetMapping("/{id}")
//    public User getById(@PathVariable String id) {
//        return userService.getById(id);
//    }

    @Operation(summary = "Get me")
    @GetMapping("/me")
    public User getMe(
            @AuthenticationPrincipal String userId
    ) {
        return userService.getById(userId);
    }
}