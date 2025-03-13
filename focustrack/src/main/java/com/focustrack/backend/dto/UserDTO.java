package com.focustrack.backend.dto;

import com.focustrack.backend.model.User;
import lombok.Getter;

@Getter
public class UserDTO {
    private Long id;
    private String email;
    private String description;

    public UserDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.description = user.getDescription();
    }
}