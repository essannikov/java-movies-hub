package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;
import ru.practicum.moviehub.types.ListOfMoviesTypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;
    private static Gson gson;

    @BeforeAll
    static void beforeAll() {
        MoviesStore moviesStore = new MoviesStore();
        moviesStore.addMovie(new Movie("Braveheart", 1995));
        moviesStore.addMovie(new Movie("The Fifth Element", 1997));

        server = new MoviesServer(moviesStore, 8080);
        server.start();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @BeforeEach
    void beforeEach() {
    }

    @AfterAll
    static void afterAll() {
        client.close();
        server.stop();
    }

    void checkContentType(HttpResponse<String> resp) {
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(BaseHttpHandler.CT_JSON, contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void getMoviesReturnsArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MoviesServer.HTTP_CONTEXT_MOVIES))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(BaseHttpHandler.DEFAULT_CHARSET));
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        checkContentType(resp);

        String body = resp.body().trim();
        String moviesStoreJson = gson.toJson(server.getMoviesStore().getMovies(), new ListOfMoviesTypeToken().getType());
        assertEquals(moviesStoreJson, body);
    }

    @Test
    void postMoviesOk() throws Exception {
        String newMovie = "{\"title\":\"Avatar\", \"year\":\"2009\"}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MoviesServer.HTTP_CONTEXT_MOVIES))
                .header("Content-Type", BaseHttpHandler.CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(newMovie))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(BaseHttpHandler.DEFAULT_CHARSET));
        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201");

        checkContentType(resp);

        String body = resp.body().trim();
        Movie movie = gson.fromJson(body, Movie.class);
        assertEquals("Avatar", movie.getTitle());
    }

    @Test
    void postMoviesTitleEmpty() throws Exception {
        String newMovie = "{\"title\":\"\", \"year\":\"2009\"}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MoviesServer.HTTP_CONTEXT_MOVIES))
                .header("Content-Type", BaseHttpHandler.CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(newMovie))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(BaseHttpHandler.DEFAULT_CHARSET));
        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

        checkContentType(resp);

        String body = resp.body().trim();
        ErrorResponse errorResponse = gson.fromJson(body, ErrorResponse.class);
        assertEquals(1, errorResponse.getDetails().length);
        assertEquals(ErrorResponse.getErrorTitle(MoviesHandler.TITLE_MAX), errorResponse.getDetails()[0]);
    }

    @Test
    void postMoviesTitleMore100() throws Exception {
        String newMovie = "{\"title\":\"" + "Name".repeat(30) + "\", \"year\":\"2009\"}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MoviesServer.HTTP_CONTEXT_MOVIES))
                .header("Content-Type", BaseHttpHandler.CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(newMovie))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(BaseHttpHandler.DEFAULT_CHARSET));
        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

        checkContentType(resp);

        String body = resp.body().trim();
        ErrorResponse errorResponse = gson.fromJson(body, ErrorResponse.class);
        assertEquals(1, errorResponse.getDetails().length);
        assertEquals(ErrorResponse.getErrorTitle(MoviesHandler.TITLE_MAX), errorResponse.getDetails()[0]);
    }

    @Test
    void postMoviesYear() throws Exception {
        String newMovie = "{\"title\":\"Avatar\", \"year\":\"999\"}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MoviesServer.HTTP_CONTEXT_MOVIES))
                .header("Content-Type", BaseHttpHandler.CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(newMovie))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(BaseHttpHandler.DEFAULT_CHARSET));
        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

        checkContentType(resp);

        String body = resp.body().trim();
        ErrorResponse errorResponse = gson.fromJson(body, ErrorResponse.class);
        assertEquals(1, errorResponse.getDetails().length);
        assertEquals(ErrorResponse.getErrorYear(MoviesHandler.YEAR_MIN, MoviesHandler.getYearMax()), errorResponse.getDetails()[0]);
    }

    @Test
    void postMoviesContentTypeError() throws Exception {
        String newMovie = "{\"title\":\"Avatar\", \"year\":\"2009\"}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MoviesServer.HTTP_CONTEXT_MOVIES))
                .header("Content-Type", "application/xml; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(newMovie))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(BaseHttpHandler.DEFAULT_CHARSET));
        assertEquals(415, resp.statusCode(), "POST /movies должен вернуть 415");
    }

    @Test
    void postMoviesJsonError() throws Exception {
        String newMovie = "\"title\"=\"Avatar\" \"year\":\"2009\"}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MoviesServer.HTTP_CONTEXT_MOVIES))
                .header("Content-Type", BaseHttpHandler.CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(newMovie))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(BaseHttpHandler.DEFAULT_CHARSET));
        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");
    }

    @Test
    void getMoviesReturnById() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MoviesServer.HTTP_CONTEXT_MOVIES + "/1"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(BaseHttpHandler.DEFAULT_CHARSET));
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        checkContentType(resp);

        String body = resp.body().trim();
        String movieJson = gson.toJson(server.getMoviesStore().getMovie(1), Movie.class);
        assertEquals(movieJson, body);
    }

    @Test
    void getMoviesReturnByIdNotFound() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MoviesServer.HTTP_CONTEXT_MOVIES + "/11"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(BaseHttpHandler.DEFAULT_CHARSET));
        assertEquals(404, resp.statusCode(), "GET /movies должен вернуть 404");

        checkContentType(resp);

        String body = resp.body().trim();
        ErrorResponse errorResponse = gson.fromJson(body, ErrorResponse.class);
        assertEquals(ErrorResponse.getErrorMovieNotFound(), errorResponse.getError());
    }

    @Test
    void getMoviesReturnByIdIncorrect() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MoviesServer.HTTP_CONTEXT_MOVIES + "/asd"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(BaseHttpHandler.DEFAULT_CHARSET));
        assertEquals(400, resp.statusCode(), "GET /movies должен вернуть 400");

        checkContentType(resp);

        String body = resp.body().trim();
        ErrorResponse errorResponse = gson.fromJson(body, ErrorResponse.class);
        assertEquals(ErrorResponse.getErrorId(), errorResponse.getError());
    }

    @Test
    void deleteMoviesById() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MoviesServer.HTTP_CONTEXT_MOVIES + "/2"))
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(BaseHttpHandler.DEFAULT_CHARSET));
        assertEquals(204, resp.statusCode(), "GET /movies должен вернуть 204");
    }

    @Test
    void deleteMoviesByIdNotFound() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MoviesServer.HTTP_CONTEXT_MOVIES + "/11"))
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(BaseHttpHandler.DEFAULT_CHARSET));
        assertEquals(404, resp.statusCode(), "GET /movies должен вернуть 404");
    }

    @Test
    void deleteMoviesByIdIncorrect() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MoviesServer.HTTP_CONTEXT_MOVIES + "/asd"))
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(BaseHttpHandler.DEFAULT_CHARSET));
        assertEquals(400, resp.statusCode(), "GET /movies должен вернуть 400");

        checkContentType(resp);

        String body = resp.body().trim();
        ErrorResponse errorResponse = gson.fromJson(body, ErrorResponse.class);
        assertEquals(ErrorResponse.getErrorId(), errorResponse.getError());
    }

    @Test
    void getMoviesForYearReturnsArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MoviesServer.HTTP_CONTEXT_MOVIES + "?year=1995"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(BaseHttpHandler.DEFAULT_CHARSET));
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        checkContentType(resp);

        String body = resp.body().trim();
        String moviesStoreJson = gson.toJson(server.getMoviesStore().getMovies(1995), new ListOfMoviesTypeToken().getType());
        assertEquals(moviesStoreJson, body);
    }

    @Test
    void getMoviesForYearReturnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MoviesServer.HTTP_CONTEXT_MOVIES + "?year=2000"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(BaseHttpHandler.DEFAULT_CHARSET));
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        checkContentType(resp);

        String body = resp.body().trim();
        assertEquals("[]", body);
    }

    @Test
    void getMoviesForYearErrorParameter() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MoviesServer.HTTP_CONTEXT_MOVIES + "?year=asd"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(BaseHttpHandler.DEFAULT_CHARSET));
        assertEquals(400, resp.statusCode(), "GET /movies должен вернуть 400");

        checkContentType(resp);

        String body = resp.body().trim();
        ErrorResponse errorResponse = gson.fromJson(body, ErrorResponse.class);
        assertEquals(ErrorResponse.getErrorParameterIncorrect("year"), errorResponse.getError());
    }

    @Test
    void putMoviesError() throws Exception {
        String newMovie = "{\"title\":\"Avatar\", \"year\":\"2009\"}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MoviesServer.HTTP_CONTEXT_MOVIES))
                .header("Content-Type", BaseHttpHandler.CT_JSON)
                .PUT(HttpRequest.BodyPublishers.ofString(newMovie))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(BaseHttpHandler.DEFAULT_CHARSET));
        assertEquals(405, resp.statusCode(), "POST /movies должен вернуть 405");
    }
}