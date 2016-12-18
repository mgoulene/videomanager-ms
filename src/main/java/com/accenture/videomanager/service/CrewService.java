package com.accenture.videomanager.service;

import com.accenture.videomanager.service.dto.CrewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Service Interface for managing Crew.
 */
public interface CrewService {

    /**
     * Save a crew.
     *
     * @param crewDTO the entity to save
     * @return the persisted entity
     */
    CrewDTO save(CrewDTO crewDTO);

    /**
     *  Get all the crews.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<CrewDTO> findAll(Pageable pageable);

    /**
     *  Get the "id" crew.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    CrewDTO findOne(Long id);

    /**
     *  Delete the "id" crew.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the crew corresponding to the query.
     *
     *  @param query the query of the search
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<CrewDTO> search(String query, Pageable pageable);
}
