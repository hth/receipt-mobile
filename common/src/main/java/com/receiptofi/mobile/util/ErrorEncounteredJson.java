package com.receiptofi.mobile.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts error message to JSON.
 * User: hitender
 * Date: 7/10/14 11:04 PM
 */
public final class ErrorEncounteredJson {

    @SuppressWarnings("unused")
    private ErrorEncounteredJson() {
    }

    public static String toJson(Map<String, String> errors) {
        JsonObject error = new JsonObject();
        for (String key : errors.keySet()) {
            error.addProperty(key, errors.get(key));
        }

        JsonObject result = new JsonObject();
        result.add("error", error);

        return new Gson().toJson(result);
    }

    public static String toJson(String reason, MobileSystemErrorCodeEnum systemErrorCode) {
        Map<String, String> errors = new HashMap<>();
        errors.put("reason", reason);
        errors.put("systemError", systemErrorCode.name());
        errors.put("systemErrorCode", systemErrorCode.getCode());
        return toJson(errors);
    }
}
