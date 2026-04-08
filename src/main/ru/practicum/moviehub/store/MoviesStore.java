package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoviesStore {
    private int idCount;
    private final Map<Integer, Movie> moviesMap;

    public MoviesStore() {
        this.idCount = 0;
        this.moviesMap = new HashMap<>();
    }

    public Movie getMovie(int id) {
        return moviesMap.get(id);
    }

    public int addMovie(Movie movie) {
        int id = nextIdCount();
        moviesMap.put(id, movie);
        return id;
    }

    public Movie deleteMovie(int id) {
        return moviesMap.remove(id);
    }

    public List<Movie> getMovies() {
        return moviesMap.values().stream().toList();
    }

    public List<Movie> getMovies(int year) {
        return moviesMap.values().stream().filter(movie -> movie.getYear() == year).toList();
    }

    protected int nextIdCount() {
        return ++idCount;
    }
}