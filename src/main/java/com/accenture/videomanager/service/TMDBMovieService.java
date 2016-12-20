package com.accenture.videomanager.service;

import com.accenture.videomanager.service.dto.MovieDTO;
import info.movito.themoviedbapi.model.MovieDb;

import java.util.List;

/**
 * Created by vagrant on 12/14/16.
 */
public interface TMDBMovieService {
    public MovieDb findOne(int tmdbId);
    public List<MovieDb> searchTMDBMovies(String query);
    public MovieDTO saveMovie(int tmbdId);
    //public MovieDTO saveTMDBMovies(int fromId, int toId);

}
