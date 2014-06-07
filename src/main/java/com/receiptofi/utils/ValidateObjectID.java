package com.receiptofi.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates Object ID
 *
 * User: hitender
 * Date: 4/15/13
 * Time: 2:36 AM
 */
public class ValidateObjectID {

    private static Pattern p = Pattern.compile("^[0-9a-fA-F]{24}$");

    public static boolean isValid(String id) {
        Matcher m = p.matcher(id);
        return m.matches();
    }
}
