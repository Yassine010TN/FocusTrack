package com.focustrack.backend.repository;

import com.focustrack.backend.model.SharedGoal;
import com.focustrack.backend.model.Goal;
import com.focustrack.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SharingRepository extends JpaRepository<SharedGoal, Long> {
    List<SharedGoal> findByContact(User contact);
    Optional<SharedGoal> findByGoalAndContact(Goal goal, User contact);
    Optional<SharedGoal> findByGoalAndOwnerAndContact(Goal goal, User owner, User contact);
    List<SharedGoal> findByGoal(Goal goal);
    List<SharedGoal> findByOwnerAndContact(User owner, User contact);
    List<SharedGoal> findByGoalAndOwner(Goal goal, User owner);
}