package com.focustrack.backend.service;

import com.focustrack.backend.model.*;
import com.focustrack.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.focustrack.backend.dto.GoalDTO;
import com.focustrack.backend.dto.UserDTO;
import com.focustrack.backend.dto.GoalCommentDTO;
import com.focustrack.backend.dto.SharedGoalDTO;

@Service
public class SharingService {

    @Autowired private GoalRepository goalRepository;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private SharingRepository sharingRepository;
    @Autowired private GoalStepRepository goalStepRepository;
    @Autowired private ContactRepository contactRepository;
    @Autowired private UserGoalRepository userGoalRepository;
    @Autowired private GoalCommentRepository goalCommentRepository;

    public List<SharedGoalDTO> getSharedGoals() {
        User user = userService.getAuthenticatedUser();
        return sharingRepository.findByContact(user)
                .stream()
                .map(sharedGoal -> new SharedGoalDTO(
                        new GoalDTO(sharedGoal.getGoal()),
                        sharedGoal.getOwner().getEmail(),
                        sharedGoal.getOwner().getId()
                ))
                .collect(Collectors.toList());
    }
    
    public List<SharedGoalDTO> getGoalsSharedByUser(Long ownerId) {
        User contact = userService.getAuthenticatedUser(); // current user
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SharedGoal> sharedGoals = sharingRepository.findByOwnerAndContact(owner, contact);

        return sharedGoals.stream()
                .map(sharedGoal -> new SharedGoalDTO(
                        new GoalDTO(sharedGoal.getGoal()),
                        sharedGoal.getOwner().getEmail(),
                        sharedGoal.getOwner().getId()
                ))
                .collect(Collectors.toList());
    }

    public List<UserDTO> getUsersSharedWith(Long goalId) {
        User owner = userService.getAuthenticatedUser();
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        // Ensure the current user owns the goal and it's a main goal
        userGoalRepository.findByUserAndGoalAndHierarchy(owner, goal, 1)
                .orElseThrow(() -> new RuntimeException("You do not own this goal or it's not a main goal"));

        List<SharedGoal> sharedGoals = sharingRepository.findByGoalAndOwner(goal, owner);

        return sharedGoals.stream()
                .map(shared -> new UserDTO(shared.getContact()))
                .collect(Collectors.toList());
    }
    
    public List<GoalDTO> getStepsOfSharedGoal(Long goalId) {
        User user = userService.getAuthenticatedUser();
        Goal mainGoal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        sharingRepository.findByGoalAndContact(mainGoal, user)
                .orElseThrow(() -> new RuntimeException("You do not have access to this goal"));

        return goalStepRepository.findByMainGoal(mainGoal)
                .stream()
                .map(step -> new GoalDTO(step.getStepGoal()))
                .collect(Collectors.toList());
    }

    public void shareGoal(Long goalId, Long contactId) {
        User owner = userService.getAuthenticatedUser();
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        userGoalRepository.findByUserAndGoalAndHierarchy(owner, goal, 1)
                .orElseThrow(() -> new RuntimeException("Only main goals can be shared"));

        User contact = userRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact user not found"));

        if (owner.getId().equals(contactId)) throw new RuntimeException("Cannot share with yourself");

        boolean areContacts = contactRepository.findBySenderAndContact(owner, contact).filter(Contact::isContactAccepted).isPresent()
                || contactRepository.findBySenderAndContact(contact, owner).filter(Contact::isContactAccepted).isPresent();

        if (!areContacts) {
            throw new RuntimeException("You can only share goals with your contacts");
        }

        Optional<SharedGoal> existing = sharingRepository.findByGoalAndOwnerAndContact(goal, owner, contact);
        if (existing.isPresent()) throw new RuntimeException("Goal already shared with this contact");

        SharedGoal sharedGoal = new SharedGoal();
        sharedGoal.setGoal(goal);
        sharedGoal.setOwner(owner);
        sharedGoal.setContact(contact);

        sharingRepository.save(sharedGoal);
    }

    public void unshareGoal(Long goalId, Long contactId) {
        User owner = userService.getAuthenticatedUser();
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        SharedGoal sharedGoal = sharingRepository.findByGoalAndOwnerAndContact(goal, owner, 
                userRepository.findById(contactId)
                        .orElseThrow(() -> new RuntimeException("Contact not found")))
                .orElseThrow(() -> new RuntimeException("Goal not shared with this contact"));

        sharingRepository.delete(sharedGoal);
    }

    public void addComment(Long goalId, String text) {
        User user = userService.getAuthenticatedUser();
        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new RuntimeException("Goal not found!"));

        boolean hasAccess = userGoalRepository.findByUserAndGoal(user, goal).isPresent() ||
                sharingRepository.findByGoalAndContact(goal, user).isPresent();

        if (!hasAccess) throw new RuntimeException("You do not have access to comment on this goal!");

        GoalComment comment = new GoalComment();
        comment.setGoal(goal);
        comment.setAuthor(user);
        comment.setContent(text);
        comment.setCreatedAt(LocalDateTime.now());

        goalCommentRepository.save(comment);
    }

    public List<GoalCommentDTO> getComments(Long goalId) {
        User user = userService.getAuthenticatedUser();
        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new RuntimeException("Goal not found!"));

        boolean hasAccess = userGoalRepository.findByUserAndGoal(user, goal).isPresent() ||
                sharingRepository.findByGoalAndContact(goal, user).isPresent();

        if (!hasAccess) throw new RuntimeException("You do not have access to view comments!");

        return goalCommentRepository.findByGoalOrderByCreatedAtAsc(goal)
                .stream()
                .map(comment -> new GoalCommentDTO(
                		comment.getId(),
                        comment.getAuthor().getEmail(),
                        comment.getCreatedAt(),
                        comment.getContent()
                ))
                .collect(Collectors.toList());
    }

    public void deleteComment(Long commentId) {
        User currentUser = userService.getAuthenticatedUser();

        GoalComment comment = goalCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        Goal goal = comment.getGoal();

        boolean isAuthor = comment.getAuthor().getId().equals(currentUser.getId());
        boolean isGoalOwner = userGoalRepository.findByUserAndGoalAndHierarchy(currentUser, goal, 1).isPresent();

        if (!isAuthor && !isGoalOwner) {
            throw new RuntimeException("You can only delete your own comments or comments on your goals");
        }

        goalCommentRepository.delete(comment);
    }

    
    public void updateComment(Long commentId, String newText) {
        User user = userService.getAuthenticatedUser();
        GoalComment comment = goalCommentRepository.findByIdAndAuthor(commentId, user)
                .orElseThrow(() -> new RuntimeException("You can only update your own comments"));
        comment.setContent(newText);
        goalCommentRepository.save(comment);
    }
}
