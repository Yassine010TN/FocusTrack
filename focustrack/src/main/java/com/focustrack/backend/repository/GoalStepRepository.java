package com.focustrack.backend.repository;

import com.focustrack.backend.model.GoalStep;
import com.focustrack.backend.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GoalStepRepository extends JpaRepository<GoalStep, Long> {
    List<GoalStep> findByMainGoal(Goal mainGoal);
    GoalStep findByMainGoalAndStepGoal(Goal mainGoal, Goal stepGoal);
    Optional<GoalStep> findByStepGoal(Goal stepGoal);
}
