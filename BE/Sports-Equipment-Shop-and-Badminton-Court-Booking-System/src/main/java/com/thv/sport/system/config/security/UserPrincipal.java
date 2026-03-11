package com.thv.sport.system.config.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal {

    private Integer userId;
    private String email;
}