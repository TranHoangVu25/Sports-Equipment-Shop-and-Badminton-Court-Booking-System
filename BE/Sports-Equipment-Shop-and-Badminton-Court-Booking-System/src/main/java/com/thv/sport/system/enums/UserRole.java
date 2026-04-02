package com.thv.sport.system.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("ROLE_admin"),
    USER("ROLE_user");
    private String value;
    UserRole(String value) {
        this.value = value;
    }
}
