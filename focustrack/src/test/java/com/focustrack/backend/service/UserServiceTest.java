package com.focustrack.backend.service;

import com.focustrack.backend.dto.RegisterUserDTO;
import com.focustrack.backend.dto.UserDTO;
import com.focustrack.backend.model.User;
import com.focustrack.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



import com.focustrack.backend.dto.UpdateUserDTO;
import com.focustrack.backend.model.Contact;
import com.focustrack.backend.repository.ContactRepository;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    
    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private UserService userService;

    private RegisterUserDTO registerUserDTO;
    private User savedUser;
    private User user;
    private User contactUser;

    @BeforeEach
    void setUp() {
        registerUserDTO = new RegisterUserDTO();
        registerUserDTO.setEmail("test@example.com");
        registerUserDTO.setPassword("password123");
        registerUserDTO.setDescription("Test user");

        savedUser = new User();
        savedUser.setId(1L); // Ensure the ID is set
        savedUser.setEmail(registerUserDTO.getEmail());
        savedUser.setPassword("encodedPassword");
        savedUser.setDescription(registerUserDTO.getDescription());
        
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");
        user.setDescription("Test user");

        contactUser = new User();
        contactUser.setId(2L);
        contactUser.setEmail("contact@example.com");
        contactUser.setPassword("encodedPassword");
        contactUser.setDescription("Friend user");
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.findByEmail(registerUserDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerUserDTO.getPassword())).thenReturn("encodedPassword");

        // Ensure save() assigns an ID like a real database would
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simulate database behavior
            return user;
        });

        UserDTO result = userService.registerUser(registerUserDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(registerUserDTO.getEmail(), result.getEmail());
        assertEquals(registerUserDTO.getDescription(), result.getDescription());

        verify(passwordEncoder, times(1)).encode(registerUserDTO.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        when(userRepository.findByEmail(registerUserDTO.getEmail())).thenReturn(Optional.of(savedUser));

        Exception exception = assertThrows(RuntimeException.class, () -> userService.registerUser(registerUserDTO));

        assertEquals("Email already registered!", exception.getMessage());

        verify(userRepository, times(1)).findByEmail(registerUserDTO.getEmail());
        verify(userRepository, never()).save(any(User.class)); // Ensure save() is never called if email exists
    }
    
    @Test
    void testLoginUser_Success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);

        User result = userService.loginUser(user.getEmail(), "password123");

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
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
    void testUpdateUser_Success() {
        UpdateUserDTO updateData = new UpdateUserDTO();
        updateData.setEmail("newemail@example.com");
        updateData.setPassword("newpassword");
        updateData.setDescription("Updated user");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(updateData.getPassword())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.updateUser(user.getId(), updateData);

        assertNotNull(result);
        assertEquals("newemail@example.com", result.getEmail());
        assertEquals("Updated user", result.getDescription());
    }

    @Test
    void testDeleteUser_Success() {
        when(userRepository.existsById(user.getId())).thenReturn(true);

        userService.deleteUser(user.getId());

        verify(userRepository, times(1)).deleteById(user.getId());
    }

    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.existsById(user.getId())).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> userService.deleteUser(user.getId()));
        assertEquals("User not found!", exception.getMessage());
    }

    @Test
    void testSendFriendRequest_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(contactUser.getId())).thenReturn(Optional.of(contactUser));
        when(contactRepository.findBySenderAndContact(user, contactUser)).thenReturn(Optional.empty());

        userService.sendFriendRequest(user.getId(), contactUser.getId());

        verify(contactRepository, times(1)).save(any(Contact.class));
    }

    @Test
    void testRespondToFriendRequest_Accept() {
        Contact friendRequest = new Contact();
        friendRequest.setSender(contactUser);
        friendRequest.setContact(user);
        friendRequest.setContactAccepted(false);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(contactUser.getId())).thenReturn(Optional.of(contactUser));
        when(contactRepository.findBySenderAndContact(contactUser, user)).thenReturn(Optional.of(friendRequest));

        userService.respondToFriendRequest(user.getId(), contactUser.getId(), true);

        assertTrue(friendRequest.isContactAccepted());
        verify(contactRepository, times(1)).save(friendRequest);
    }

    @Test
    void testRespondToFriendRequest_Reject() {
        Contact friendRequest = new Contact();
        friendRequest.setSender(contactUser);
        friendRequest.setContact(user);
        friendRequest.setContactAccepted(false);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(contactUser.getId())).thenReturn(Optional.of(contactUser));
        when(contactRepository.findBySenderAndContact(contactUser, user)).thenReturn(Optional.of(friendRequest));

        userService.respondToFriendRequest(user.getId(), contactUser.getId(), false);

        verify(contactRepository, times(1)).delete(friendRequest);
    }


}
