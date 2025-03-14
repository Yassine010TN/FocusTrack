package com.focustrack.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDTO {
    private String email;
    private String password;
    private String description;
}
