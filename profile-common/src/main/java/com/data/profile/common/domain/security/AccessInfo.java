package com.data.profile.common.domain.security;

import lombok.Data;

import java.util.Set;

@Data
public class AccessInfo {
    private String userName;
    private Set<String> userGroups;
    private Set<String> userRoles;
}
