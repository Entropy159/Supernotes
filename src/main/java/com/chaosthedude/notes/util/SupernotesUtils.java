package com.chaosthedude.notes.util;

import com.chaosthedude.notes.config.NotesConfig;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;

import java.util.Objects;

import static com.chaosthedude.notes.Supernotes.*;

public class SupernotesUtils {
    public static boolean hasApiKey() {
        boolean hasKey = !Objects.equals(NotesConfig.apiKey, "API_KEY_HERE");
        if (!hasKey) {
            LOGGER.warn("No API key set!");
        }
        return hasKey;
    }

    public static String getApiKey() {
        return hasApiKey() ? NotesConfig.apiKey : "";
    }

    public static HttpResponse<JsonNode> get(String url) {
        return Unirest.get(baseUrl + url).header("Api-Key", getApiKey()).asJson();
    }

    public static HttpResponse<JsonNode> post(String url, String body) {
        return Unirest.post(baseUrl + url).header("Api-Key", getApiKey()).header("Content-Type", "application/json").body(body).asJson();
    }

    public static HttpResponse<JsonNode> patch(String url, String body) {
        return Unirest.patch(baseUrl + url).header("Api-Key", getApiKey()).header("Content-Type", "application/json").body(body).asJson();
    }
}
