package ru.practicum.moviehub.api;


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

    public static String getErrorTitle(int lengthMax) {
        return "Название должно быть не пустой строкой, длиной до " + lengthMax + " символов";
    }

    public static String getErrorYear(int yearMin, int yearMax) {
        return "Год принимает значения от " + yearMin + " до " + yearMax;
    }

    public static String getErrorValidation() {
        return "Ошибка валидации";
    }
}