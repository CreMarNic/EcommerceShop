package org.example.backend;

import org.example.backend.dto.UserCreateRequest;
import org.example.backend.dto.UserResponse;
import org.example.backend.exceptions.APIException;
import org.example.backend.model.User;
import org.example.backend.repository.UserRepository;
import org.example.backend.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplUnitTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void createUserHashesPasswordAndSavesUser() {

        // given: a new user request and no existing user with the same email.

        UserCreateRequest request = new UserCreateRequest("Maria", "maria@example.com", "secret123");

        when(userRepository.findByEmail("maria@example.com")).thenReturn(null);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-password");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userBeingSaved = invocation.getArgument(0);
            userBeingSaved.setId(1L);
            return userBeingSaved;
        });

        // when: the service creates the user.

        UserResponse response = userService.createUser(request);

        // then: the response has public user data, and the password was encoded before saving.

        assertEquals(1L, response.getId());
        assertEquals("Maria", response.getName());
        assertEquals("maria@example.com", response.getEmail());

        verify(userRepository).findByEmail("maria@example.com");
        verify(passwordEncoder).encode("secret123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserThrowsExceptionWhenEmailAlreadyExists() {

        // given: the repository says this email already belongs to another user.

        UserCreateRequest request = new UserCreateRequest("Maria", "maria@example.com", "secret123");
        User existingUser = new User(1L, "Existing Maria", "maria@example.com", "old-password");

        when(userRepository.findByEmail("maria@example.com")).thenReturn(existingUser);

        // when / then: creating another user with the same email should fail.

        assertThrows(APIException.class, () -> userService.createUser(request));

        verify(userRepository).findByEmail("maria@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
}
