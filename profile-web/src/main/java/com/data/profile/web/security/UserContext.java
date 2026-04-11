package com.data.profile.web.security;

import com.data.profile.web.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContext {
    User user;
    AccessInfo accessInfo;
}
