package com.focustrack.backend.repository;

import com.focustrack.backend.model.Contact;
import com.focustrack.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findBySenderAndContactAcceptedFalse(User user); // 🔹 Get pending invitations
    List<Contact> findBySenderAndContactAcceptedTrue(User user);  // 🔹 Get accepted contacts
    Optional<Contact> findBySenderAndContact(User user, User contact); // 🔹 Find specific contact entry
    List<Contact> findByContactAndContactAcceptedFalse(User contact);
    List<Contact> findBySenderOrContactAndContactAcceptedTrue(User user, User contact);
}
