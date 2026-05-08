package com.re.it210project.model.entity;

import com.re.it210project.model.enums.Role;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionUser {

    private Long id;

    private String username;

    private String fullName;

    private String email;

    private Role role;
}