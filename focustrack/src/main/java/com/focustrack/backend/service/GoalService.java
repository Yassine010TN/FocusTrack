package com.focustrack.backend.service;

import com.focustrack.backend.model.*;
import com.focustrack.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.focustrack.backend.dto.GoalDTO;
import com.focustrack.backend.dto.GoalStepDTO;
import com.focustrack.backend.dto.UpdateGoalDTO;

@Service
public class GoalService {
    
    private final GoalRepository goalRepository;
    private final UserGoalRepository userGoalRepository;
    private final GoalStepRepository goalStepRepository;
    private final UserService userService;

    @Autowired
    public GoalService(GoalRepository goalRepository, UserGoalRepository userGoalRepository, 
                        GoalStepRepository goalStepRepository, UserService userService) {
        this.goalRepository = goalRepository;
        this.userGoalRepository = userGoalRepository;
        this.goalStepRepository = goalStepRepository;
        this.userService = userService;
    }

    // Create a new goal (hierarchy 1)
    public Goal createGoal(String description, int priority, LocalDate startDate, LocalDate dueDate, int order) {
        User user = userService.getAuthenticatedUser();
        Goal goal = new Goal(description, priority, startDate, dueDate, order);
        goal = goalRepository.save(goal);

        UserGoal userGoal = new UserGoal();
        userGoal.setUser(user);
        userGoal.setGoal(goal);
        userGoal.setHierarchy(1);  // Hierarchy 1 (Main Goal)
        userGoalRepository.save(userGoal);

        return goal;
    }

    // Add a sub-goal (hierarchy 2)
    public Goal addGoalStep(Long mainGoalId, String description, int priority, LocalDate startDate, LocalDate dueDate, int stepOrder) {
    	User user = userService.getAuthenticatedUser();
        Goal mainGoal = goalRepository.findById(mainGoalId)
                .orElseThrow(() -> new RuntimeException("Main Goal not found!"));

        Goal stepGoal = new Goal(description, priority, startDate, dueDate, stepOrder); 
        stepGoal = goalRepository.save(stepGoal);

        GoalStep goalStep = new GoalStep();
        goalStep.setMainGoal(mainGoal);
        goalStep.setStepGoal(stepGoal);
        goalStepRepository.save(goalStep);
        
        UserGoal userGoal = new UserGoal();
        userGoal.setUser(user);
        userGoal.setGoal(stepGoal);
        userGoal.setHierarchy(2);  // Hierarchy 2 (step of a goal)
        userGoalRepository.save(userGoal);

        return stepGoal;
    }

 // Modify a goal's completion status (done/not done)
    public void updateGoalStatus(Long goalId, boolean isDone) {
        User user = userService.getAuthenticatedUser();
        Goal goalToUpdated = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found!"));
    	UserGoal userGoal = userGoalRepository.findByUserAndGoal(user, goalToUpdated)
            .orElseThrow(() -> new RuntimeException("Goal not assigned to user!"));


        goalToUpdated.setDone(isDone);
        goalRepository.save(goalToUpdated);
    }

    public void updateGoal(Long goalId, UpdateGoalDTO updateData) {
        User user = userService.getAuthenticatedUser();
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found!"));

        // Ensure the user owns the goal
        UserGoal userGoal = userGoalRepository.findByUserAndGoal(user, goal)
                .orElseThrow(() -> new RuntimeException("Goal not assigned to user!"));

        // Update fields if provided
        if (updateData.getDescription() != null) {
            goal.setDescription(updateData.getDescription());
        }
        if (updateData.getPriority() != null) {
            goal.setPriority(updateData.getPriority());
        }
        if (updateData.getProgress() != null) {
            goal.setProgress(updateData.getProgress());
        }
        if (updateData.getStartDate() != null) {
            goal.setStartDate(updateData.getStartDate());
        }
        if (updateData.getDueDate() != null) {
            goal.setDueDate(updateData.getDueDate());
        }

        if (updateData.getIsDone() != null) {
            goal.setDone(updateData.getIsDone());
        }
        goalRepository.save(goal);

    }

    public List<GoalDTO> getUserGoals() {
        User user = userService.getAuthenticatedUser();
        List<UserGoal> userGoals = userGoalRepository.findByUserAndHierarchy(user, 1);

        return userGoals.stream()
                .map(userGoal -> new GoalDTO(userGoal.getGoal()))
                .collect(Collectors.toList());
    }

    public List<GoalStepDTO> getGoalSteps(Long goalId) {
        User user = userService.getAuthenticatedUser();

        // Ensure the main goal belongs to the user
        Goal mainGoal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found!"));

        userGoalRepository.findByUserAndGoal(user, mainGoal)
                .orElseThrow(() -> new RuntimeException("Goal not assigned to user!"));

        // Retrieve steps (sub-goals) of the main goal and map to DTO
        return goalStepRepository.findByMainGoal(mainGoal)
                .stream()
                .map(GoalStepDTO::new) // ✅ Convert GoalStep to GoalStepDTO
                .collect(Collectors.toList());
    }
    
    public void deleteGoal(Long goalId) {
        User user = userService.getAuthenticatedUser();
        Goal mainGoal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found!"));

        // Ensure the user owns the goal
        userGoalRepository.findByUserAndGoalAndHierarchy(user, mainGoal, 1)
                .orElseThrow(() -> new RuntimeException("Goal not assigned to user or it's not a main Goal!"));

        // Retrieve all step goals linked to this main goal
        List<GoalStep> goalSteps = goalStepRepository.findByMainGoal(mainGoal);

        // Extract step goals and delete them first
        List<Goal> stepGoals = goalSteps.stream()
                .map(GoalStep::getStepGoal)
                .collect(Collectors.toList());
        
        // Delete step goals from the userGoal table first
        List<UserGoal> stepUserGoals = userGoalRepository.findByGoalIn(stepGoals);
        userGoalRepository.deleteAll(stepUserGoals);
        
        goalStepRepository.deleteAll(goalSteps); // Delete step relationships
        goalRepository.deleteAll(stepGoals); // Delete step goals

        // Finally, delete the main goal
        userGoalRepository.deleteAll(userGoalRepository.findByUserAndGoal(user, mainGoal).stream().toList());
        goalRepository.delete(mainGoal);
    }


    public void deleteStepGoal(Long stepGoalId) {
        User user = userService.getAuthenticatedUser();
        
        // Find the step goal
        Goal stepGoal = goalRepository.findById(stepGoalId)
                .orElseThrow(() -> new RuntimeException("Step Goal not found!"));

        // Ensure the user owns the goal
        userGoalRepository.findByUserAndGoalAndHierarchy(user, stepGoal, 2)
                .orElseThrow(() -> new RuntimeException("Goal not assigned to user or it's not a main Goal!"));

        // Find the GoalStep relation to get the main goal
        GoalStep goalStep = goalStepRepository.findByStepGoal(stepGoal)
                .orElseThrow(() -> new RuntimeException("Step Goal is not linked to a main goal!"));

        Goal mainGoal = goalStep.getMainGoal();

        // ✅ Ensure the user owns the **main goal** (not the step goal)
        userGoalRepository.findByUserAndGoalAndHierarchy(user, mainGoal, 1)
                .orElseThrow(() -> new RuntimeException("You do not own the main goal, cannot delete step!"));

        userGoalRepository.findByUserAndGoal(user, stepGoal).ifPresent(userGoalRepository::delete);

        // ✅ Remove from goal_step table
        goalStepRepository.delete(goalStep);

        // ✅ Delete the step goal itself
        goalRepository.delete(stepGoal);
    }
    
}
