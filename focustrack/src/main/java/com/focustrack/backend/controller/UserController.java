package com.focustrack.backend.controller;

import com.focustrack.backend.dto.UserDTO;
import com.focustrack.backend.dto.ContactDTO;
import com.focustrack.backend.model.Contact;
import com.focustrack.backend.model.User;
import com.focustrack.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody User user) {
        UserDTO newUser = userService.registerUser(user);
        return ResponseEntity.ok(newUser);
    }

    //  Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            return ResponseEntity.ok(userService.loginUser(email, password));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //  Get User by Email (Now returns only ID & Email)
    @GetMapping("/search")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        try {
            return ResponseEntity.ok(userService.getUserByEmail(email));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }   

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }       
    
    //  Update Profile
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestParam String description) {
        try {
            return ResponseEntity.ok(userService.updateUser(id, description));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //  Delete Profile
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    //  Send Friend Request
    @PostMapping("/invite")
    public ResponseEntity<?> sendFriendRequest(@RequestParam Long userId, @RequestParam Long contactId) {
        try {
            userService.sendFriendRequest(userId, contactId);
            return ResponseEntity.ok("Friend request sent successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //  Accept/Reject Friend Request
    @PostMapping("/respond-invite")
    public ResponseEntity<?> respondToFriendRequest(@RequestParam Long userId, @RequestParam Long contactId, @RequestParam boolean accept) {
        try {
            userService.respondToFriendRequest(userId, contactId, accept);
            return ResponseEntity.ok(accept ? "Friend request accepted!" : "Friend request declined!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

 //  Get Sent Invitations (requests this user sent)
    @GetMapping("/invitations/sent")
    public ResponseEntity<?> getSentInvitations(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(userService.getSentInvitations(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


 //  Get Received Invitations (requests this user received)
    @GetMapping("/invitations/received")
    public ResponseEntity<?> getReceivedInvitations(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(userService.getReceivedInvitations(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //  Get Contacts List (Accepted Friends)
    @GetMapping("/contacts")
    public ResponseEntity<?> getContacts(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(userService.getContacts(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); 
        }
    }   
}

