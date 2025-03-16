package com.focustrack.backend.service;

import com.focustrack.backend.dto.UserDTO;
import com.focustrack.backend.dto.RegisterUserDTO;
import com.focustrack.backend.dto.UpdateUserDTO;
import com.focustrack.backend.model.User;
import com.focustrack.backend.model.Contact;
import com.focustrack.backend.repository.UserRepository;

import jakarta.validation.Valid;

import com.focustrack.backend.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import com.focustrack.backend.security.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, ContactRepository contactRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            
            Long userId = Long.parseLong(jwt.getSubject()); //  Extract user ID

            return userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        throw new RuntimeException("Unauthorized request");
    }
    

    public UserDTO registerUser(@Valid RegisterUserDTO userDTO) { //  Validates fields
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered!");
        }
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword())); // Encrypt password
        user.setDescription(userDTO.getDescription());

        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser);
    }
    
    public String loginUser(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            return jwtUtil.generateToken(user.get().getId(), email); //  Pass user ID and email
        }
        throw new RuntimeException("Invalid credentials!");
    }


    //  Get User by Email 
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return new UserDTO(user);
    }    
    
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + id));
        return new UserDTO(user);    
    }
    
    public UserDTO getCurrentUserInfo() {
    	User user = getAuthenticatedUser();
        return new UserDTO(user);    
    }
    
    public UserDTO updateUser(UpdateUserDTO updateData) {
    	User currentUser = getAuthenticatedUser();
        return userRepository.findById(currentUser.getId())
            .map(user -> {
                //  Update email if provided
                if (updateData.getEmail() != null && !updateData.getEmail().isEmpty()) {
                    if (userRepository.findByEmail(updateData.getEmail()).isPresent() && (! updateData.getEmail().equalsIgnoreCase(user.getEmail()))) {
                    	throw new RuntimeException("Email already in use!");
                    }
                    user.setEmail(updateData.getEmail());
                }

                //  Update password if provided (hashed)
                if (updateData.getPassword() != null && !updateData.getPassword().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(updateData.getPassword()));
                }

                //  Update description if provided
                if (updateData.getDescription() != null) {
                    user.setDescription(updateData.getDescription());
                }

                //  Save changes & return updated user as DTO
                User savedUser = userRepository.save(user);
                return new UserDTO(savedUser);
            }).orElseThrow(() -> new RuntimeException("User not found!"));
    }


    public void deleteUser() {
    	User currentUser = getAuthenticatedUser();
        userRepository.deleteById(currentUser.getId());
    }
    
    public void deleteContact(Long contactId) {
    	User user = getAuthenticatedUser();
    	if (user.getId() == contactId) {
    		throw new RuntimeException("You can't send a request to yourself!");
    	}
        User contactUser = userRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact user not found"));

        Optional<Contact> contactToBeDeleted = contactRepository.findBySenderAndContact(user, contactUser);
        Optional<Contact> contactToBeDeleted2 = contactRepository.findBySenderAndContact(contactUser, user);

        if (contactToBeDeleted.isPresent()) {
            contactRepository.delete(contactToBeDeleted.get());
        } else if (contactToBeDeleted2.isPresent()) {
            contactRepository.delete(contactToBeDeleted2.get());
        } else {
            throw new RuntimeException("No contact found between the users.");
        }
    }

    public void sendFriendRequest(Long contactId) {
    	User user = getAuthenticatedUser();
    	if (user.getId() == contactId) {
    		throw new RuntimeException("You can't send a request to yourself!");
    	}
        User contact = userRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact user not found"));

        if (contactRepository.findBySenderAndContact(user, contact).isPresent() || contactRepository.findBySenderAndContact(contact, user).isPresent()) {
            throw new RuntimeException("Friend request already sent or user is already a contact!");
        }

        Contact newContact = new Contact();
        newContact.setSender(user);
        newContact.setContact(contact);
        newContact.setContactAccepted(false);
        contactRepository.save(newContact);
    }
    
    //  Accept/Reject Friend Request
    public void respondToFriendRequest(Long contactId, boolean accept) {
    	User user = getAuthenticatedUser();
    	if (user.getId() == contactId) {
    		throw new RuntimeException("You can't send a request to yourself!");
    	}
        User contact = userRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact user not found"));

        Contact friendRequest = contactRepository.findBySenderAndContact(contact, user)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (accept) {
            friendRequest.setContactAccepted(true);
            contactRepository.save(friendRequest);
        } else {
            contactRepository.delete(friendRequest);
        }
    }
    
    //  Get Sent Invitations (requests this user sent)
    public List<UserDTO> getSentInvitations() {
    	User user = getAuthenticatedUser();
        //  Find all sent Invitations to new contacts
        List<Contact> sentInvitations = contactRepository.findBySenderAndContactAcceptedFalse(user);

        //  Convert to UserDTO list
        return sentInvitations.stream()
                .map(contact -> {
                    // Determine the friend: If user was the sender, return receiver. Otherwise, return sender.
                    User contactUser = contact.getContact();
                    return new UserDTO(contactUser);
                })
                .collect(Collectors.toList());
    }

    //  Get Received Invitations (requests this user received)
    public List<UserDTO> getReceivedInvitations() {
    	User user = getAuthenticatedUser();


        //  Find all sent Invitations to new contacts
        List<Contact> receivedInvitations = contactRepository.findByContactAndContactAcceptedFalse(user);

        //  Convert to UserDTO list
        return receivedInvitations.stream()
                .map(contact -> {
                    User contactUser = contact.getSender();
                    return new UserDTO(contactUser);
                })
                .collect(Collectors.toList());
    }

    //  Get List of Users (Accepted Contacts)
    public List<UserDTO> getContacts() {
        User user = getAuthenticatedUser(); 

        List<Contact> contactsList = contactRepository.findAcceptedContacts(user);

        return contactsList.stream()
                .map(contact -> {
                    User contactUser = contact.getSender().getId().equals(user.getId()) ? contact.getContact() : contact.getSender();
                    return new UserDTO(contactUser);
                })
                .collect(Collectors.toList());
    }

}
