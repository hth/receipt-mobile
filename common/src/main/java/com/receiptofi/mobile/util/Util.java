package com.receiptofi.mobile.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * User: hitender
 * Date: 5/9/16 9:20 PM
 */
public class Util {

    public static List<String> convertCommaSeparatedStringToList(final String commaSeparatedString) {
        List<String> fidList = new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(commaSeparatedString, " ,");
        while (stringTokenizer.hasMoreTokens()) {
            fidList.add(stringTokenizer.nextToken());
        }
        return fidList;
    }
}
