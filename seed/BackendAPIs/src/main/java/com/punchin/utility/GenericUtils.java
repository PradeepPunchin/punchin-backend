package com.punchin.utility;

import com.punchin.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class GenericUtils {
    /**
     * To Get the current logged in user.
     */
    public static User getLoggedInUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
