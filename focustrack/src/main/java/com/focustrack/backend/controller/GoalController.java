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

@RestController
@RequestMapping("/goals")
public class GoalController {
    
    private final GoalService goalService;

    @Autowired
    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createGoal(@RequestParam String description, @RequestParam int priority,
                                        @RequestParam LocalDate startDate, @RequestParam LocalDate dueDate, @RequestParam int order) {
        Goal goal = goalService.createGoal(description, priority, startDate, dueDate, order);
        return ResponseEntity.ok(goal);
    }

    @PostMapping("/{mainGoalId}/addStep")
    public ResponseEntity<?> addGoalStep(@PathVariable Long mainGoalId, @RequestParam String description,
                                         @RequestParam int priority, @RequestParam LocalDate startDate,
                                         @RequestParam LocalDate dueDate, @RequestParam int stepOrder) {
        Goal stepGoal = goalService.addGoalStep(mainGoalId, description, priority, startDate, dueDate, stepOrder);
        return ResponseEntity.ok(stepGoal);
    }

 // ✅ PATCH API to mark goal as done or not done
    @PatchMapping("/{goalId}/status")
    public ResponseEntity<?> updateGoalStatus(@PathVariable Long goalId, @RequestParam boolean isDone) {
        goalService.updateGoalStatus(goalId, isDone);
        return ResponseEntity.ok("Goal status updated successfully!");
    }

    @PatchMapping("/{goalId}")
    public ResponseEntity<?> updateGoal(@PathVariable Long goalId, @RequestBody UpdateGoalDTO updateData) {
        goalService.updateGoal(goalId, updateData);
        return ResponseEntity.ok("Goal updated successfully!");
    }

    @GetMapping("/my")
    public ResponseEntity<List<GoalDTO>> getUserGoals() {
        return ResponseEntity.ok(goalService.getUserGoals());
    }




    @GetMapping("/{goalId}/steps")
    public ResponseEntity<List<GoalStepDTO>> getGoalSteps(@PathVariable Long goalId) {
        return ResponseEntity.ok(goalService.getGoalSteps(goalId));
    }

    
    @DeleteMapping("/{goalId}")
    public ResponseEntity<?> deleteGoal(@PathVariable Long goalId) {
        goalService.deleteGoal(goalId);
        return ResponseEntity.ok("Goal deleted successfully!");
    }
    
    @DeleteMapping("/steps/{stepGoalId}")
    public ResponseEntity<?> deleteStepGoal(@PathVariable Long stepGoalId) {
        goalService.deleteStepGoal(stepGoalId);
        return ResponseEntity.ok("Step goal deleted successfully!");
    }


}
