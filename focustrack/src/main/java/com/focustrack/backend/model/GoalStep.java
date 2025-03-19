package com.focustrack.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "goal_steps")
public class GoalStep {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_goal_id", nullable = false)
    private Goal mainGoal; // Reference to Main Goal (Hierarchy 1)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_goal_id", nullable = false)
    private Goal stepGoal; // Reference to Step Goal (Hierarchy 2)


}
