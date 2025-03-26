package com.focustrack.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SharedGoalDTO {
    private GoalDTO goal;
    private String ownerEmail;
    private long ownerID;
}
