package com.focustrack.backend.controller;

import com.focustrack.backend.model.Goal;
import com.focustrack.backend.model.GoalComment;
import com.focustrack.backend.service.SharingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sharing")
public class SharingController {

    @Autowired private SharingService sharingService;

    @GetMapping("/shared-goals")
    public ResponseEntity<List<?>> getGoalsSharedWithMe() {
        return ResponseEntity.ok(sharingService.getSharedGoals());
    }

    @GetMapping("/shared/from/{ownerId}")
    public ResponseEntity<List<?>> getGoalsSharedByUser(@PathVariable Long ownerId) {
        List<?> sharedGoals = sharingService.getGoalsSharedByUser(ownerId);
        return ResponseEntity.ok(sharedGoals);
    }

    @GetMapping("/shared/{goalId}/users")
    public ResponseEntity<List<?>> getUsersSharedWith(@PathVariable Long goalId) {
        List<?> users = sharingService.getUsersSharedWith(goalId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/shared-goals/{goalId}/steps")
    public ResponseEntity<List<?>> getStepsOfSharedGoal(@PathVariable Long goalId) {
        return ResponseEntity.ok(sharingService.getStepsOfSharedGoal(goalId));
    }

    @PostMapping("/share")
    public ResponseEntity<?> shareGoal(@RequestParam Long goalId, @RequestParam Long contactId) {
        sharingService.shareGoal(goalId, contactId);
        return ResponseEntity.ok("Goal shared successfully!");
    }

    @DeleteMapping("/unshare")
    public ResponseEntity<?> unshareGoal(@RequestParam Long goalId, @RequestParam Long contactId) {
        sharingService.unshareGoal(goalId, contactId);
        return ResponseEntity.ok("Goal unshared successfully!");
    }

    @PostMapping("/comment")
    public ResponseEntity<?> commentGoal(@RequestParam Long goalId, @RequestParam String text) {
        sharingService.addComment(goalId, text);
        return ResponseEntity.ok("Comment added!");
    }

    @GetMapping("/comments/{goalId}")
    public ResponseEntity<List<?>> getComments(@PathVariable Long goalId) {
        return ResponseEntity.ok(sharingService.getComments(goalId));
    }

    @DeleteMapping("/my-comment/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        sharingService.deleteComment(commentId);
        return ResponseEntity.ok("Comment deleted successfully!");
    }

    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<?> deleteOtherComment(@PathVariable Long commentId) {
        sharingService.deleteOtherComment(commentId);
        return ResponseEntity.ok("Comment deleted successfully!");
    }
    
    @PatchMapping("/comment/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId, @RequestParam String newText) {
        sharingService.updateComment(commentId, newText);
        return ResponseEntity.ok("Comment updated successfully!");
    }
}
