package com.focustrack.backend.dto;

import com.focustrack.backend.model.Goal;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class GoalDTO {
    private Long id;
    private String description;
    private int priority;
    private int progress;
    private LocalDate startDate;
    private LocalDate dueDate;
    private int hierarchy;
    private boolean isDone; // Include isDone from UserGoal

    public GoalDTO(Goal goal) {
        this.id = goal.getId();
        this.description = goal.getDescription();
        this.priority = goal.getPriority();
        this.progress = goal.getProgress();
        this.startDate = goal.getStartDate();
        this.dueDate = goal.getDueDate();
        this.isDone = goal.isDone();
    }
}
