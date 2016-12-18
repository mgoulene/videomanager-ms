package com.accenture.videomanager.service;

import com.accenture.videomanager.service.dto.PictureDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Service Interface for managing Picture.
 */
public interface PictureService {

    /**
     * Save a picture.
     *
     * @param pictureDTO the entity to save
     * @return the persisted entity
     */
    PictureDTO save(PictureDTO pictureDTO);

    /**
     *  Get all the pictures.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<PictureDTO> findAll(Pageable pageable);

    /**
     *  Get the "id" picture.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    PictureDTO findOne(Long id);

    /**
     *  Delete the "id" picture.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the picture corresponding to the query.
     *
     *  @param query the query of the search
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<PictureDTO> search(String query, Pageable pageable);
}
