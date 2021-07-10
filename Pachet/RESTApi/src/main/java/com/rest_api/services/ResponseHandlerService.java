package com.rest_api.services;

import java.util.HashMap;
import java.util.Map;

/**
 * <ul>
 * 	<li>Clasa care va expune functionalitatile necesare crearii corpului raspunsurilor cererilor HTTP.</li>
 * 	<li> Corpul raspunsului va fi in format <strong>JSON</strong>.</li>
 * 	<li>Cheile vor reprezenta tipul raspunsului <strong>success</strong>, <strong>eroare</strong> sau <strong>custom</strong>.</li>
 * 	<li>Clasa nu va fi instantiata, deci fiecare metoda va fi statica.</li>
 * </ul>
 */
public class ResponseHandlerService{
    /**
     * <ul>
     * 	<li>Functia de creare a raspunsului de eroare.</li>
     * 	<li> Cheia : <strong>error status</strong>.</li>
     * </ul>
     */
    public static Map<String, String> buildErrorStatus(String errorMessage) {
        return new HashMap<String, String>(){{
            put("error status", errorMessage);
        }};
    }

    /**
     * <ul>
     * 	<li>Functia de creare a raspunsului de succes.</li>
     * 	<li> Cheia : <strong>success status</strong>.</li>
     * </ul>
     */
    public static Map<String, String> buildSuccessStatus(String statusMessage) {
        return new HashMap<String, String>(){{
            put("success status", statusMessage);
        }};
    }

    /**
     * <ul>
     * 	<li>Functia de creare a raspunsului custom</li>
     * 	<li>Cheile si valorile vor fi definite de program.</li>
     * </ul>
     */
    public static Map<String, Object> buildCustomResponse(String key, Object value) {
        return new HashMap<String, Object>(){{
            put(key, value);
        }};
    }
}
