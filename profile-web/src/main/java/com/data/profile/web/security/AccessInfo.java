package com.data.profile.web.security;

import lombok.Data;

import java.util.Set;

@Data
public class AccessInfo {
    private String userName;
    private Set<String> userGroups;
    private Set<String> userRoles;
}
