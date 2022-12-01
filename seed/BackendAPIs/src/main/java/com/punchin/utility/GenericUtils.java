package com.punchin.utility;

import com.punchin.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

public class GenericUtils {
    /**
     * To Get the current logged in user.
     */
    public static User getLoggedInUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static boolean checkExcelFormat(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            return contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        return false;
    }
}
