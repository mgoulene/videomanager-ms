package com.accenture.videomanager.service;

import com.accenture.videomanager.service.dto.MovieDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Service Interface for managing Movie.
 */
public interface MovieService {

    /**
     * Save a movie.
     *
     * @param movieDTO the entity to save
     * @return the persisted entity
     */
    MovieDTO save(MovieDTO movieDTO);

    /**
     *  Get all the movies.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<MovieDTO> findAll(Pageable pageable);

    /**
     *  Get the "id" movie.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    MovieDTO findOne(Long id);

    /**
     *  Delete the "id" movie.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the movie corresponding to the query.
     *
     *  @param query the query of the search
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<MovieDTO> search(String query, Pageable pageable);

    public MovieDTO findOneByTmdbId(int tmbdId);
}
