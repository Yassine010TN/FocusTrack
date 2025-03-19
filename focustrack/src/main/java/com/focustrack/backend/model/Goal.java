package com.focustrack.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "goals")
public class Goal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int priority; // Must be between 1-10

    @Column(nullable = false)
    private int progress; // Percentage completion (0-100)

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate dueDate;
    
    @Column(nullable = false)
    private boolean isDone = false; // Default false (not completed)
    
    @Column(nullable = false)
    private int goalOrder; // Order of steps for sorting

    public Goal(String description, int priority, LocalDate startDate, LocalDate dueDate, int goalOrder) {
        this.description = description;
        this.priority = priority;
        this.progress = 0; // Default to 0% progress
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.isDone = false;
        this.goalOrder = goalOrder;
    }
}
