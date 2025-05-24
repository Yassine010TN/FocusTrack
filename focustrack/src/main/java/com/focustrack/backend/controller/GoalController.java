package com.focustrack.backend.controller;

import com.focustrack.backend.model.Goal;
import com.focustrack.backend.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.focustrack.backend.dto.GoalDTO;
import com.focustrack.backend.dto.UpdateGoalDTO;
import com.focustrack.backend.dto.GoalStepDTO;

import java.util.List;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/goals")
@Tag(name = "Goals", description = "Manage user goals and sub-goals (steps)")
public class GoalController {

    private final GoalService goalService;

    @Autowired
    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @Operation(summary = "Create a new goal", description = "Creates a new main goal for the authenticated user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Goal created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/")
    public ResponseEntity<?> createGoal(
            @RequestParam String description,
            @RequestParam int priority,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate dueDate,
            @RequestParam int order) {
        Goal goal = goalService.createGoal(description, priority, startDate, dueDate, order);
        return ResponseEntity.ok(goal);
    }

    @Operation(summary = "Add a sub-goal (step) to a main goal", description = "Adds a new step goal under a main goal")
    @PostMapping("/{mainGoalId}/steps")
    public ResponseEntity<?> addGoalStep(
            @PathVariable Long mainGoalId,
            @RequestParam String description,
            @RequestParam int priority,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate dueDate,
            @RequestParam int stepOrder) {
        Goal stepGoal = goalService.addGoalStep(mainGoalId, description, priority, startDate, dueDate, stepOrder);
        return ResponseEntity.ok(stepGoal);
    }

    @Operation(summary = "Update a goal's done status", description = "Mark a goal as done or not done")
    @PatchMapping("/{goalId}/status")
    public ResponseEntity<?> updateGoalStatus(@PathVariable Long goalId, @RequestParam boolean isDone) {
        goalService.updateGoalStatus(goalId, isDone);
        return ResponseEntity.ok("Goal status updated successfully!");
    }

    @Operation(summary = "Update a goal", description = "Updates the fields of a goal including description, progress, priority, etc.")
    @PatchMapping("/{goalId}")
    public ResponseEntity<?> updateGoal(@PathVariable Long goalId, @RequestBody UpdateGoalDTO updateData) {
        goalService.updateGoal(goalId, updateData);
        return ResponseEntity.ok("Goal updated successfully!");
    }

    @Operation(summary = "Get current user's goals", description = "Returns all main goals created by the authenticated user")
    @GetMapping("/")
    public ResponseEntity<List<GoalDTO>> getUserGoals() {
        return ResponseEntity.ok(goalService.getUserGoals());
    }

    @Operation(summary = "Get a specific goal", description = "Returns details of a specific goal owned by the user")
    @GetMapping("/{goalId}")
    public ResponseEntity<GoalDTO> getMyGoal(@PathVariable Long goalId) {
        GoalDTO goalDTO = goalService.getMyGoal(goalId);
        return ResponseEntity.ok(goalDTO);
    }

    @Operation(summary = "Get steps of a goal", description = "Returns the sub-goals (steps) associated with a main goal")
    @GetMapping("/{goalId}/steps")
    public ResponseEntity<List<GoalStepDTO>> getGoalSteps(@PathVariable Long goalId) {
        return ResponseEntity.ok(goalService.getGoalSteps(goalId));
    }

    @Operation(summary = "Delete a goal", description = "Deletes a main goal and its associated steps")
    @DeleteMapping("/{goalId}")
    public ResponseEntity<?> deleteGoal(@PathVariable Long goalId) {
        goalService.deleteGoal(goalId);
        return ResponseEntity.ok("Goal deleted successfully!");
    }

    @Operation(summary = "Delete a step goal", description = "Deletes a sub-goal (step) from a main goal")
    @DeleteMapping("/steps/{stepGoalId}")
    public ResponseEntity<?> deleteStepGoal(@PathVariable Long stepGoalId) {
        goalService.deleteStepGoal(stepGoalId);
        return ResponseEntity.ok("Step goal deleted successfully!");
    }
}
