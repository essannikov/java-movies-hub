package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class BaseHttpHandler implements HttpHandler {
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final String CT_JSON = "application/json; charset=UTF-8";

    protected void sendJson(HttpExchange ex, int status, String json) throws IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status, 0);
        ex.getResponseBody().write(json.getBytes(DEFAULT_CHARSET));
    }

    protected void sendNoContent(HttpExchange ex, int status) throws java.io.IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status, -1);
    }

    protected boolean checkHeaderContentTypeIsJson(HttpExchange ex) {
        String contentType = ex.getRequestHeaders().getFirst("Content-Type");
        return contentType != null && contentType.equalsIgnoreCase(CT_JSON);
    }

    protected String[] getPathSplit(HttpExchange ex) {
        return ex.getRequestURI().getPath().split("/");
    }

    protected Map<String, String> getPathParameters(HttpExchange ex) {
        Map<String, String> result = new HashMap<>();

        String query = ex.getRequestURI().getQuery();
        if (query == null || query.isBlank()) {
            return result;
        }

        for (String param: query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(URLDecoder.decode(entry[0], DEFAULT_CHARSET),
                        URLDecoder.decode(entry[1], DEFAULT_CHARSET));
            } else {
                result.put(URLDecoder.decode(entry[0], DEFAULT_CHARSET), "");
            }
        }

        return result;
    }

    protected Optional<Integer> convertStringToInt(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }
}