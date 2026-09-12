package com.king.ledgerengine.domain.account;

import com.king.ledgerengine.domain.account.dto.CreateAccountDto;
import com.king.ledgerengine.domain.account.entity.Account;
import com.king.ledgerengine.domain.account.enums.AccountOwnerType;
import com.king.ledgerengine.domain.account.enums.AccountType;
import com.king.ledgerengine.domain.entry.EntryRepository;
import com.king.ledgerengine.domain.user.UserRepository;
import com.king.ledgerengine.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Unit Tests")
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private EntryRepository entryRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;

    private CreateAccountDto testCreateAccountDto;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("johndoe@example.com")
                .password("password")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now().plusDays(1))
                .build();

        testAccount = Account.builder()
                .id("account-123")
                .type(AccountType.ASSET)
                .name("Test Account")
                .ownerType(AccountOwnerType.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now().plusDays(1))
                .user(testUser)
                .build();

        testCreateAccountDto = CreateAccountDto.builder()
                .name("Test Account")
                .type(AccountType.ASSET)
                .build();
    }

//    @Nested
//    @DisplayName("Create Account Tests")
//    class CreateAccountTests {
//        @Test
//        @DisplayName("Should create account successfully when valid payload and userId exists")
//        void shouldCreateAccountSuccessfully() {
//            // Given
//            final String userId = "user-123";
//
//            when(userRepository.findById(userId))
//                    .thenReturn(Optional.of(testUser));
//            when(accountRepository.save(testAccount)).thenReturn(testAccount);
//
//            // When
//            final Account result = accountService.create(testCreateAccountDto, userId);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(userId, result.getUser().getId());
//            verify(userRepository, times(1)).findById(userId);
//            verify(accountRepository, times(1)).save(testAccount);
//        }
//    }

    @Nested
    @DisplayName("Get Account Tests")
    class GetAccountTests {

        @Test
        @DisplayName("Should get one account successfully when provided with the correct arguments")
        void shouldGetOneAccountSuccessfully() {
            // Given
            final String userId = "user-123";
            when(accountRepository.findByIdAndUserId(testAccount.getId(), userId))
                    .thenReturn(Optional.of(testAccount));
            // When
            Account result = accountService.getOne(userId, testAccount.getId());

            // Then
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when Account is not found")
        void shouldThrowResponseStatusExceptionWhenAccountIsNotFound() {
            // Given
            final String userId = "user-123";

            when(accountRepository.findByIdAndUserId(testAccount.getId(), userId))
                    .thenReturn(Optional.empty());


            // When & Then
            final ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> accountService.getOne(userId, testAccount.getId())
            );

            assertEquals("Account not found or does not belong to this user", exception.getReason());
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        }

    }

}