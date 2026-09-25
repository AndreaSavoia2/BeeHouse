package com.prj.beehouse.util;

import org.springframework.context.annotation.Bean;

import java.security.SecureRandom;
import java.util.Base64;

public class StringUtility {

    /**
     * Normalizes the given string by lowercasing and trimming it.
     * <p>
     * Null-safe: returns {@code null} when the input is {@code null}.
     *
     * @param string the string to normalize, may be null
     * @return the normalized string, or {@code null} if the input was null
     */
    public static String cleanString(String string) {
        if (string == null) {
            return null;
        }
        return string.toLowerCase().trim();
    }

    /**
     * Normalizes the given string by uppercasing and trimming it.
     * <p>
     * Null-safe: returns {@code null} when the input is {@code null}.
     *
     * @param string the string to normalize, may be null
     * @return the normalized string, or {@code null} if the input was null
     */
    public static String cleanStringUpper(String string){
        if (string == null) {
            return null;
        }
        return string.toUpperCase().trim();
    }


    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}