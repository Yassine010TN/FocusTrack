package com.focustrack.backend.repository;

import com.focustrack.backend.model.Contact;
import com.focustrack.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findBySenderAndContactAcceptedFalse(User user); // 🔹 Get pending invitations
    List<Contact> findBySenderAndContactAcceptedTrue(User user);  // 🔹 Get accepted contacts
    Optional<Contact> findBySenderAndContact(User user, User contact); // 🔹 Find specific contact entry
    List<Contact> findByContactAndContactAcceptedFalse(User contact);

    @Query("SELECT c FROM Contact c WHERE (c.sender = :user OR c.contact = :user) AND c.contactAccepted = true")
    List<Contact> findAcceptedContacts(@Param("user") User user);
}
