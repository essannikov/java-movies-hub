package ru.practicum.moviehub.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class MovieJson {
    private JsonObject jsonObject;

    public MovieJson(String json) throws JsonSyntaxException {
        JsonElement jsonElement = JsonParser.parseString(json);
        if (jsonElement.isJsonObject()) {
            this.jsonObject = jsonElement.getAsJsonObject();
        }
    }

    public String getTitle() {
        String title = null;

        if (jsonObject.has("title") && !jsonObject.get("title").isJsonNull()) {
            title = jsonObject.get("title").getAsString();
        }

        return title;
    }

    public Integer getYear() {
        Integer year = null;

        if (jsonObject.has("year") && !jsonObject.get("year").isJsonNull()) {
            try {
                year = jsonObject.get("year").getAsInt();
            } catch (NumberFormatException | IllegalStateException e) {
            }
        }

        return year;
    }
}
