package com.focustrack.backend.service;

import com.focustrack.backend.dto.RegisterUserDTO;
import com.focustrack.backend.dto.UpdateUserDTO;
import com.focustrack.backend.dto.UserDTO;
import com.focustrack.backend.model.User;
import com.focustrack.backend.repository.UserRepository;
import com.focustrack.backend.repository.ContactRepository;
import com.focustrack.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserService userService;

    private RegisterUserDTO registerUserDTO;
    private User user;
    private String fakeToken = "fake-jwt-token";

    @BeforeEach
    void setUp() {
        registerUserDTO = new RegisterUserDTO();
        registerUserDTO.setEmail("test@example.com");
        registerUserDTO.setPassword("password123");
        registerUserDTO.setDescription("Test user");

        user = new User();
        user.setId(1L);
        user.setEmail(registerUserDTO.getEmail());
        user.setPassword("encodedPassword");
        user.setDescription(registerUserDTO.getDescription());

    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.findByEmail(registerUserDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerUserDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        UserDTO result = userService.registerUser(registerUserDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(registerUserDTO.getEmail(), result.getEmail());

        verify(passwordEncoder, times(1)).encode(registerUserDTO.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        when(userRepository.findByEmail(registerUserDTO.getEmail())).thenReturn(Optional.of(user));

        Exception exception = assertThrows(RuntimeException.class, () -> userService.registerUser(registerUserDTO));

        assertEquals("Email already registered!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginUser_Success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(user.getId(), user.getEmail())).thenReturn(fakeToken);

        String token = userService.loginUser(user.getEmail(), "password123");

        assertNotNull(token);
        assertEquals(fakeToken, token);
    }

    @Test
    void testLoginUser_InvalidCredentials() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", user.getPassword())).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> userService.loginUser(user.getEmail(), "wrongpassword"));

        assertEquals("Invalid credentials!", exception.getMessage());
    }

    @Test
    void testGetUserByEmail_Success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserByEmail(user.getEmail());

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(user.getId());

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
    }



    @Test
    void testDeleteUser_NotFound() {
        // Mock the security context with a fake JWT
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        Jwt mockJwt = mock(Jwt.class);
        when(authentication.getPrincipal()).thenReturn(mockJwt);
        when(mockJwt.getSubject()).thenReturn(String.valueOf(user.getId()));

        // Simulate the user is not found in the repository
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> userService.deleteUser());

        assertEquals("User not found", exception.getMessage());
    }

}
