package com.accenture.videomanager.service;

import com.accenture.videomanager.service.dto.MovieDTO;
import info.movito.themoviedbapi.model.MovieDb;

import java.util.List;

/**
 * Interface to retrieve Data from TMDB
 */
public interface TMDBMovieService {
    /**
     * Find Movie data from TMDB
     *
     * @param tmdbId the id of the movie
     * @return the movie data
     */
    MovieDb findOne(int tmdbId);

    /**
     * Find movies from a quesry
     *
     * @param query the movie query
     * @return list of movie data
     */
    List<MovieDb> searchTMDBMovies(String query);

    /**
     * Save movie data from a TMDB movie id
     *
     * @param tmbdId the id of the movie
     * @return the saved DTO of the movie
     */
    MovieDTO saveMovie(int tmbdId);

}
