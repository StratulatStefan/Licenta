package com.safestorage.services;

import java.util.HashMap;
import java.util.Map;

public class ResponseHandlerService{
    public static Map<String, String> buildErrorStatus(String errorMessage) {
        return new HashMap<String, String>(){{
            put("error status", errorMessage);
        }};
    }

    public static Map<String, String> buildSuccessStatus(String statusMessage) {
        return new HashMap<String, String>(){{
            put("success status", statusMessage);
        }};
    }

    public static Map<String, Object> buildCustomResponse(String key, Object value) {
        return new HashMap<String, Object>(){{
            put(key, value);
        }};
    }
}

