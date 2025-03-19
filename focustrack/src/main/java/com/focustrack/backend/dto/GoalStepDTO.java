package com.focustrack.backend.dto;

import com.focustrack.backend.model.Goal;
import com.focustrack.backend.model.GoalStep;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class GoalStepDTO {
    private Long stepGoalId;
    private String description;
    private int priority;
    private int progress;
    private LocalDate startDate;
    private LocalDate dueDate;
    private int hierarchy;
    private int stepOrder; // ✅ Order of the step
    private boolean isDone;

    public GoalStepDTO(GoalStep goalStep) {
        Goal stepGoal = goalStep.getStepGoal();
        this.stepGoalId = stepGoal.getId();
        this.description = stepGoal.getDescription();
        this.priority = stepGoal.getPriority();
        this.progress = stepGoal.getProgress();
        this.startDate = stepGoal.getStartDate();
        this.dueDate = stepGoal.getDueDate();
        this.stepOrder = stepGoal.getGoalOrder();
        this.isDone =  stepGoal.isDone();
    }
}
