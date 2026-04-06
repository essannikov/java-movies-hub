package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    private MoviesStore moviesStore;
    private final int port;
    private final HttpServer server;

    public MoviesServer(MoviesStore moviesStore, int port) {
        this.moviesStore = moviesStore;
        this.port = port;

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/movies", new MoviesHandler(this.moviesStore));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать HTTP-сервер", e);
        }
    }

    public void start() {
        if (server == null) {
            return;
        }
        server.start();
        System.out.println("Сервер запущен");
    }

    public void stop() {
        if (server == null) {
            return;
        }
        server.stop(0);
        System.out.println("Сервер остановлен");
    }

    public MoviesStore getMoviesStore() {
        return moviesStore;
    }

    public void setMoviesStore(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
    }
}