package com.focustrack.backend.service;

import com.focustrack.backend.dto.UserDTO;
import com.focustrack.backend.dto.ContactDTO;
import com.focustrack.backend.model.User;
import com.focustrack.backend.model.Contact;
import com.focustrack.backend.repository.UserRepository;
import com.focustrack.backend.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, ContactRepository contactRepository) {
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public UserDTO registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encrypt password
        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser);
    }

    public User loginUser(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            return user.get();
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
    
    public UserDTO updateUser(Long id, String description) {
        return userRepository.findById(id)
            .map(user -> {
                user.setDescription(description);
                User savedUser = userRepository.save(user);
                return new UserDTO(savedUser);
            }).orElseThrow(() -> new RuntimeException("User not found!"));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found!");
        }
        userRepository.deleteById(id);
    }

    public void sendFriendRequest(Long userId, Long contactId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
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
    public void respondToFriendRequest(Long userId, Long contactId, boolean accept) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
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
    public List<ContactDTO> getSentInvitations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return contactRepository.findBySenderAndContactAcceptedFalse(user)
                .stream()
                .map(ContactDTO::new)
                .collect(Collectors.toList());
    }

    //  Get Received Invitations (requests this user received)
    public List<ContactDTO> getReceivedInvitations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return contactRepository.findByContactAndContactAcceptedFalse(user)
                .stream()
                .map(ContactDTO::new)
                .collect(Collectors.toList());
    }

    //  Get List of Users (Accepted Contacts)
    public List<UserDTO> getContacts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Find all accepted contacts (sent or received)
        List<Contact> contactsList = contactRepository.findBySenderOrContactAndContactAcceptedTrue(user, user);

        //  Convert to UserDTO list
        return contactsList.stream()
                .map(contact -> {
                    // Determine the friend: If user was the sender, return receiver. Otherwise, return sender.
                    User friend = contact.getSender().getId().equals(userId) ? contact.getContact() : contact.getSender();
                    return new UserDTO(friend);
                })
                .collect(Collectors.toList());
    }
}
