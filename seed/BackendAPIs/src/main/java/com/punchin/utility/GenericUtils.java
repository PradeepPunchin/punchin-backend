package com.punchin.utility;

import com.punchin.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

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
            if(contentType.equalsIgnoreCase("text/csv")){
                return true;
            }
            if(contentType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")){
                return true;
            }
            return false;
        }
        return false;
    }

    public static List<String> hasMatchingSubstring2(String str, List<String> substrings) {
        return substrings.stream().filter(str::contains).collect(Collectors.toList());
    }

    public static boolean hasMatchingSubstring1(String str, List<String> substrings) {
        return substrings.stream().anyMatch(str::contains);
    }
    public static String checkCSV(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return null;
        }
        if (contentType.equalsIgnoreCase("text/csv")) {
            return "csv";
        }
        return "xlSheet";
    }

}
