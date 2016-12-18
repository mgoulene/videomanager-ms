package com.accenture.videomanager.service.impl;

import com.accenture.videomanager.service.MovieService;
import com.accenture.videomanager.domain.Movie;
import com.accenture.videomanager.repository.MovieRepository;
import com.accenture.videomanager.repository.search.MovieSearchRepository;
import com.accenture.videomanager.service.dto.MovieDTO;
import com.accenture.videomanager.service.mapper.MovieMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Movie.
 */
@Service
@Transactional
public class MovieServiceImpl implements MovieService{

    private final Logger log = LoggerFactory.getLogger(MovieServiceImpl.class);

    @Inject
    private MovieRepository movieRepository;

    @Inject
    private MovieMapper movieMapper;

    @Inject
    private MovieSearchRepository movieSearchRepository;

    /**
     * Save a movie.
     *
     * @param movieDTO the entity to save
     * @return the persisted entity
     */
    public MovieDTO save(MovieDTO movieDTO) {
        log.debug("Request to save Movie : {}", movieDTO);
        Movie movie = movieMapper.movieDTOToMovie(movieDTO);
        movie = movieRepository.save(movie);
        MovieDTO result = movieMapper.movieToMovieDTO(movie);
        movieSearchRepository.save(movie);
        return result;
    }

    /**
     *  Get all the movies.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<MovieDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Movies");
        Page<Movie> result = movieRepository.findAll(pageable);
        return result.map(movie -> movieMapper.movieToMovieDTO(movie));
    }

    /**
     *  Get one movie by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public MovieDTO findOne(Long id) {
        log.debug("Request to get Movie : {}", id);
        Movie movie = movieRepository.findOneWithEagerRelationships(id);
        MovieDTO movieDTO = movieMapper.movieToMovieDTO(movie);
        return movieDTO;
    }

    /**
     *  Delete the  movie by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Movie : {}", id);
        movieRepository.delete(id);
        movieSearchRepository.delete(id);
    }

    /**
     * Search for the movie corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<MovieDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Movies for query {}", query);
        Page<Movie> result = movieSearchRepository.search(queryStringQuery(query), pageable);
        return result.map(movie -> movieMapper.movieToMovieDTO(movie));
    }

    @Transactional(readOnly = true)
    public MovieDTO findOneByTmdbId(int tmbdId) {
        log.debug("Request to get Movie from TmbdId: {}", tmbdId);
        Movie movie = movieRepository.findOneByTmdbId(tmbdId);
        MovieDTO movieDTO = movieMapper.movieToMovieDTO(movie);
        return movieDTO;
    }

}
