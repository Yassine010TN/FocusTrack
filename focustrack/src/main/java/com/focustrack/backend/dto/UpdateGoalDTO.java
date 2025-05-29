package com.focustrack.backend.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateGoalDTO {
    private String description;
    private Integer priority;
    private Integer progress;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Boolean isDone;
    private Integer goalOrder;
}
