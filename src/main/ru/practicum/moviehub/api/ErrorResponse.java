package ru.practicum.moviehub.api;

import java.time.LocalDate;

public class ErrorResponse {
    private final String error;
    private final String[] details;

    public ErrorResponse(String error, String[] details) {
        this.error = error;
        this.details = details;
    }

    public ErrorResponse(String error) {
        this.error = error;
        this.details = new String[0];
    }

    public String getError() {
        return error;
    }

    public String[] getDetails() {
        return details;
    }

    public static String getErrorParameterIncorrect(String parameter) {
        return "Некорректный параметр запроса — '" + parameter + "'";
    }

    public static String getErrorMovieNotFound() {
        return "Фильм не найден";
    }

    public static String getErrorId() {
        return "Некорректный ID";
    }

    public static String getErrorPath() {
        return "Некорректный путь";
    }

    public static String getErrorTitle() {
        return "Название должно быть не пустой строкой, длиной до 100 символов";
    }

    public static String getErrorYear() {
        int yearEnd = LocalDate.now().getYear() + 1;
        return "Год принимает значения от 1888 до " + yearEnd;
    }

    public static String getErrorValidation() {
        return "Ошибка валидации";
    }
}