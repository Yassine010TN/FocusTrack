package com.focustrack.backend.repository;

import com.focustrack.backend.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {
	
//    List<Goal> findByHierarchy(int hierarchy);
//    Optional<Goal> findByIdAndHierarchy(long id, int hierarchy);
}
