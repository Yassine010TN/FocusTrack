package com.focustrack.backend.controller;

import com.focustrack.backend.dto.UserDTO;
import com.focustrack.backend.dto.ContactDTO;
import com.focustrack.backend.dto.RegisterUserDTO;
import com.focustrack.backend.dto.UpdateUserDTO;
import com.focustrack.backend.model.Contact;
import com.focustrack.backend.model.User;
import com.focustrack.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @Operation(summary = "Register a new user", description = "Creates a new user account with email and password validation.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid email or password format")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUserDTO userDTO) {
        try {
            return ResponseEntity.ok(userService.registerUser(userDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "User Login", description = "Authenticates a user with email and password.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "400", description = "Invalid email or password")
    })

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            String token = userService.loginUser(email, password);
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @Operation(summary = "Find User by Email", description = "Retrieves user details based on the provided email.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "400", description = "User not found")
    })

    @GetMapping("/search")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        try {
            return ResponseEntity.ok(userService.getUserByEmail(email));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }   

    @Operation(summary = "Find User by ID", description = "Retrieves user details based on the provided user ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "400", description = "User not found")
    })
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }       
 
    @Operation(summary = "Update User Profile", description = "Allows users to update their email, password, or description.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid email or password format")
    })
    @SecurityRequirement(name = "BearerAuth")
    @PatchMapping("/update/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody UpdateUserDTO updateData) {
        try {
            return ResponseEntity.ok(userService.updateUser(id, updateData));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Delete User", description = "Deletes the user account and removes associated data.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(responseCode = "400", description = "User not found")
    })

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @Operation(summary = "Send Friend Request", description = "Allows a user to send a friend request to another user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Friend request sent successfully"),
        @ApiResponse(responseCode = "400", description = "User not found or request already sent")
    })

    @PostMapping("/invite")
    public ResponseEntity<?> sendFriendRequest(@RequestParam Long userId, @RequestParam Long contactId) {
        try {
            userService.sendFriendRequest(userId, contactId);
            return ResponseEntity.ok("Friend request sent successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Respond to Friend Request", description = "Accept or decline a pending friend request.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Friend request accepted/declined"),
        @ApiResponse(responseCode = "400", description = "Friend request not found")
    })

    @PostMapping("/respond-invite")
    public ResponseEntity<?> respondToFriendRequest(@RequestParam Long userId, @RequestParam Long contactId, @RequestParam boolean accept) {
        try {
            userService.respondToFriendRequest(userId, contactId, accept);
            return ResponseEntity.ok(accept ? "Friend request accepted!" : "Friend request declined!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Get Sent Friend Requests", description = "Retrieves the list of friend requests sent by the user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of sent friend requests"),
        @ApiResponse(responseCode = "400", description = "User not found")
    })

    @GetMapping("/invitations/sent")
    public ResponseEntity<?> getSentInvitations(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(userService.getSentInvitations(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @Operation(summary = "Get Received contact Requests", description = "Retrieves the list of contact requests received by the user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of received friend requests"),
        @ApiResponse(responseCode = "400", description = "User not found")
    })

    @GetMapping("/invitations/received")
    public ResponseEntity<?> getReceivedInvitations(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(userService.getReceivedInvitations(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Get User's contatcs List", description = "Retrieves a list of accepted contacts.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of contacts retrieved"),
        @ApiResponse(responseCode = "400", description = "User not found")
    })
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/contacts")
    public ResponseEntity<?> getContacts(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(userService.getContacts(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); 
        }
    }
    
    @Operation(summary = "Remove Contact", description = "Removes an accepted contact from the user's contacts list.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contact removed successfully"),
        @ApiResponse(responseCode = "400", description = "Contact not found")
    })
   
    @DeleteMapping("/contacts/remove")
    public ResponseEntity<?> deleteContact(@RequestParam Long userId, @RequestParam Long contactId) {
        try {
            userService.deleteContact(userId, contactId);
            return ResponseEntity.ok("Contact removed successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // Return 400 Bad Request if contact not found
        }
    }

}

