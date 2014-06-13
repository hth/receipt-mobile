package com.receiptofi.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: hitender
 * Date: 1/21/14 7:17 PM
 */
public final class ParseJsonStringToMap {

    public static Map<String, String> jsonStringToMap(String ids) throws IOException {
        return new ObjectMapper().readValue(ids, new TypeReference<HashMap<String,String>>() {});
    }
}
