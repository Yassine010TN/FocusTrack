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
public class UserServiceTest2 {

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

        // Mock authentication context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mock(Jwt.class));
        when(((Jwt) authentication.getPrincipal()).getSubject()).thenReturn(String.valueOf(user.getId()));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    }



    @Test
    void testGetAuthenticatedUser_Success() {
        User authenticatedUser = userService.getAuthenticatedUser();

        assertNotNull(authenticatedUser);
        assertEquals(user.getId(), authenticatedUser.getId());
    }

    @Test
    void testUpdateUser_Success() {
        UpdateUserDTO updateData = new UpdateUserDTO();
        updateData.setEmail("newemail@example.com");
        updateData.setPassword("newpassword");
        updateData.setDescription("Updated user");

        when(passwordEncoder.encode(updateData.getPassword())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.updateUser(updateData);

        assertNotNull(result);
        assertEquals("newemail@example.com", result.getEmail());
        assertEquals("Updated user", result.getDescription());
    }

    @Test
    void testDeleteUser_Success() {
        userService.deleteUser();

        verify(userRepository, times(1)).deleteById(user.getId());
    }

    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> userService.deleteUser());

        assertEquals("User not found!", exception.getMessage());
    }
}
