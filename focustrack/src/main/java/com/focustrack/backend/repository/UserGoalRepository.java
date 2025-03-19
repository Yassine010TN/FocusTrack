package com.focustrack.backend.repository;

import com.focustrack.backend.model.UserGoal;
import com.focustrack.backend.model.User;
import com.focustrack.backend.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserGoalRepository extends JpaRepository<UserGoal, Long> {
    List<UserGoal> findByUserAndHierarchy(User user, int hierarchy);
    Optional<UserGoal> findByUserAndGoal(User user, Goal goal);
    Optional<UserGoal> findByUserAndGoalAndHierarchy(User user, Goal goal, int hierarchy);
    List<UserGoal> findByGoalIn(List<Goal> goals);
}
