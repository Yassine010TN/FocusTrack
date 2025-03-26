package com.focustrack.backend.repository;

import com.focustrack.backend.model.Goal;
import com.focustrack.backend.model.GoalComment;
import com.focustrack.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalCommentRepository extends JpaRepository<GoalComment, Long> {
    List<GoalComment> findByGoalOrderByCreatedAtAsc(Goal goal);
    Optional<GoalComment> findByIdAndAuthor(Long id, User author);
}