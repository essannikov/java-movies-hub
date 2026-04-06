package ru.practicum.moviehub.http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.api.MovieJson;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;
import ru.practicum.moviehub.types.ListOfMoviesTypeToken;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore moviesStore;
    private final Gson gson;

    public MoviesHandler(MoviesStore moviesStore) {
        super();

        this.moviesStore = moviesStore;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();

        if (method.equalsIgnoreCase("GET")) {
            handleGet(ex);
        } else if (method.equalsIgnoreCase("POST")) {
            handlePost(ex);
        } else if (method.equalsIgnoreCase("DELETE")) {
            handleDelete(ex);
        } else {
            sendNoContent(ex, 405);
        }

        ex.close();
    }

    protected void handleGet(HttpExchange ex) throws IOException {
        String[] paths = getPathSplit(ex);
        Map<String, String> parameters = getPathParameters(ex);

        if (paths.length == 2) {
            if (parameters.isEmpty()) {
                handleGetMovies(ex);
            } else {
                handleGetMoviesWithParams(ex, parameters);
            }
        } else if (paths.length == 3) {
            handleGetMoviesById(ex, paths[2]);
        } else {
            handleErrorPath(ex);
        }
    }

    protected void handleGetMovies(HttpExchange ex) throws IOException {
        String movieListJson = gson.toJson(moviesStore.getMovies(), new ListOfMoviesTypeToken().getType());
        sendJson(ex, 200, movieListJson);
    }

    protected void handleGetMoviesWithParams(HttpExchange ex, Map<String, String> parameters) throws IOException {
        String answer;

        Optional<Integer> yearOpt = convertStringToInt(parameters.get("year"));
        if (yearOpt.isEmpty()) {
            answer = gson.toJson(new ErrorResponse(ErrorResponse.getErrorParameterIncorrect("year")), ErrorResponse.class);
            sendJson(ex, 400, answer);
        } else {
            answer = gson.toJson(moviesStore.getMovies(yearOpt.get()), new ListOfMoviesTypeToken().getType());
            sendJson(ex, 200, answer);
        }
    }

    protected void handleGetMoviesById(HttpExchange ex, String idStr) throws IOException {
        String answer;

        Optional<Integer> idOpt = convertStringToInt(idStr);
        if (idOpt.isEmpty()) {
            answer = gson.toJson(new ErrorResponse(ErrorResponse.getErrorId()), ErrorResponse.class);
            sendJson(ex, 400, answer);
        } else {
            Movie movie = moviesStore.getMovie(idOpt.get());
            if (movie == null) {
                answer = gson.toJson(new ErrorResponse(ErrorResponse.getErrorMovieNotFound()), ErrorResponse.class);
                sendJson(ex, 404, answer);
            } else {
                answer = gson.toJson(movie, Movie.class);
                sendJson(ex, 200, answer);
            }
        }
    }

    protected void handlePost(HttpExchange ex) throws IOException {
        String[] paths = getPathSplit(ex);

        if (paths.length == 2) {
            handlePostMovie(ex);
        } else {
            handleErrorPath(ex);
        }
    }

    protected void handlePostMovie(HttpExchange ex) throws IOException {
        String answer;

        if (!checkHeaderContentTypeIsJson(ex)) {
            sendNoContent(ex, 415);
            return;
        }

        try {
            MovieJson movieJson = new MovieJson(new String(ex.getRequestBody().readAllBytes(), DEFAULT_CHARSET));
            String title = movieJson.getTitle();
            Integer year = movieJson.getYear();

            List<String> errorList = getMovieErrors(title, year);
            if (errorList.isEmpty()) {
                int id = moviesStore.addMovie(title, year);
                answer = gson.toJson(moviesStore.getMovie(id), Movie.class);
                sendJson(ex, 201, answer);
            } else {
                answer = gson.toJson(new ErrorResponse(ErrorResponse.getErrorValidation(),
                        errorList.toArray(String[]::new)), ErrorResponse.class);
                sendJson(ex, 422, answer);
            }
        } catch (JsonSyntaxException e) {
            answer = gson.toJson(new ErrorResponse(ErrorResponse.getErrorValidation()), ErrorResponse.class);
            sendJson(ex, 422, answer);
        }
    }

    protected List<String> getMovieErrors(String title, Integer year) {
        List<String> result = new ArrayList<>();

        if (!checkMovieTitle(title)) {
            result.add(ErrorResponse.getErrorTitle());
        }
        if (!checkMovieYear(year)) {
            result.add(ErrorResponse.getErrorYear());
        }

        return result;
    }

    protected boolean checkMovieTitle(String title) {
        return title != null && !title.isBlank() && title.length() <= 100;
    }

    protected boolean checkMovieYear(Integer year) {
        int yearEnd = LocalDate.now().getYear() + 1;
        return year != null && year >= 1888 && year <= yearEnd;
    }

    protected void handleDelete(HttpExchange ex) throws IOException {
        String[] paths = getPathSplit(ex);
        String answer;

        if (paths.length == 3) {
            Optional<Integer> idOpt = convertStringToInt(paths[2]);
            if (idOpt.isEmpty()) {
                answer = gson.toJson(new ErrorResponse(ErrorResponse.getErrorId()), ErrorResponse.class);
                sendJson(ex, 400, answer);
            } else {
                Movie movie = moviesStore.deleteMovie(idOpt.get());
                if (movie == null) {
                    sendNoContent(ex, 404);
                } else {
                    sendNoContent(ex, 204);
                }
            }
        } else {
            handleErrorPath(ex);
        }
    }

    protected void handleErrorPath(HttpExchange ex) throws IOException {
        String answer = gson.toJson(new ErrorResponse(ErrorResponse.getErrorPath()), ErrorResponse.class);
        sendJson(ex, 400, answer);
    }
}