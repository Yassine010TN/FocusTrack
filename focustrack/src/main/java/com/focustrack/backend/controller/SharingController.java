package com.focustrack.backend.controller;

import com.focustrack.backend.service.SharingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/sharing")
@Tag(name = "Sharing", description = "Manage sharing of goals between contacts, and comments")
public class SharingController {

    @Autowired
    private SharingService sharingService;

    @Operation(summary = "Get goals shared with the current user")
    @ApiResponse(responseCode = "200", description = "List of shared goals retrieved successfully")
    @GetMapping("/my-shared-goals")
    public ResponseEntity<List<?>> getGoalsSharedWithMe() {
        return ResponseEntity.ok(sharingService.getSharedGoals());
    }

    @Operation(summary = "Get goals shared by a specific user")
    @ApiResponse(responseCode = "200", description = "List of goals shared by the user")
    @GetMapping("/shared-goals")
    public ResponseEntity<List<?>> getGoalsSharedByUser(@RequestParam Long ownerId) {
        List<?> sharedGoals = sharingService.getGoalsSharedByUser(ownerId);
        return ResponseEntity.ok(sharedGoals);
    }

    @Operation(summary = "Get users a specific goal is shared with")
    @ApiResponse(responseCode = "200", description = "List of users the goal is shared with")
    @GetMapping("/shared/{goalId}/users")
    public ResponseEntity<List<?>> getUsersSharedWith(@PathVariable Long goalId) {
        List<?> users = sharingService.getUsersSharedWith(goalId);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get steps of a shared goal")
    @ApiResponse(responseCode = "200", description = "Steps of the shared goal retrieved successfully")
    @GetMapping("/shared-goals/{goalId}/steps")
    public ResponseEntity<List<?>> getStepsOfSharedGoal(@PathVariable Long goalId) {
        return ResponseEntity.ok(sharingService.getStepsOfSharedGoal(goalId));
    }

    @Operation(summary = "Share a goal with a contact")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Goal shared successfully"),
        @ApiResponse(responseCode = "400", description = "Validation or permission error")
    })
    @PostMapping("/share")
    public ResponseEntity<?> shareGoal(@RequestParam Long goalId, @RequestParam Long contactId) {
        sharingService.shareGoal(goalId, contactId);
        return ResponseEntity.ok("Goal shared successfully!");
    }

    @Operation(summary = "Unshare a goal from a contact")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Goal unshared successfully"),
        @ApiResponse(responseCode = "400", description = "Goal was not shared or permission denied")
    })
    @DeleteMapping("/share/{goalId}/{contactId}")
    public ResponseEntity<?> unshareGoal(@PathVariable Long goalId, @PathVariable Long contactId) {
        sharingService.unshareGoal(goalId, contactId);
        return ResponseEntity.ok("Goal unshared successfully!");
    }


    @Operation(summary = "Add a comment to a goal")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Comment added successfully"),
        @ApiResponse(responseCode = "400", description = "User does not have permission to comment")
    })
    @PostMapping("/comment")
    public ResponseEntity<?> commentGoal(@RequestParam Long goalId, @RequestParam String text) {
        sharingService.addComment(goalId, text);
        return ResponseEntity.ok("Comment added!");
    }

    @Operation(summary = "Get all comments on a goal")
    @ApiResponse(responseCode = "200", description = "List of comments returned")
    @GetMapping("/comments/{goalId}")
    public ResponseEntity<List<?>> getComments(@PathVariable Long goalId) {
        return ResponseEntity.ok(sharingService.getComments(goalId));
    }


    @Operation(summary = "Delete a comment", description = "Delete your own comment or comments on your own goals.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Not authorized to delete this comment")
    })
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        try {
            sharingService.deleteComment(commentId);
            return ResponseEntity.ok("Comment deleted successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @Operation(summary = "Update your comment")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Comment updated successfully"),
        @ApiResponse(responseCode = "400", description = "User is not the owner of the comment")
    })
    @PatchMapping("/comment/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId, @RequestParam String newText) {
        sharingService.updateComment(commentId, newText);
        return ResponseEntity.ok("Comment updated successfully!");
    }
}
