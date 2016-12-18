package com.accenture.videomanager.service.impl;

import com.accenture.videomanager.service.PictureService;
import com.accenture.videomanager.domain.Picture;
import com.accenture.videomanager.repository.PictureRepository;
import com.accenture.videomanager.repository.search.PictureSearchRepository;
import com.accenture.videomanager.service.dto.PictureDTO;
import com.accenture.videomanager.service.mapper.PictureMapper;
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
 * Service Implementation for managing Picture.
 */
@Service
@Transactional
public class PictureServiceImpl implements PictureService{

    private final Logger log = LoggerFactory.getLogger(PictureServiceImpl.class);
    
    @Inject
    private PictureRepository pictureRepository;

    @Inject
    private PictureMapper pictureMapper;

    @Inject
    private PictureSearchRepository pictureSearchRepository;

    /**
     * Save a picture.
     *
     * @param pictureDTO the entity to save
     * @return the persisted entity
     */
    public PictureDTO save(PictureDTO pictureDTO) {
        log.debug("Request to save Picture : {}", pictureDTO);
        Picture picture = pictureMapper.pictureDTOToPicture(pictureDTO);
        picture = pictureRepository.save(picture);
        PictureDTO result = pictureMapper.pictureToPictureDTO(picture);
        pictureSearchRepository.save(picture);
        return result;
    }

    /**
     *  Get all the pictures.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true) 
    public Page<PictureDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Pictures");
        Page<Picture> result = pictureRepository.findAll(pageable);
        return result.map(picture -> pictureMapper.pictureToPictureDTO(picture));
    }

    /**
     *  Get one picture by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true) 
    public PictureDTO findOne(Long id) {
        log.debug("Request to get Picture : {}", id);
        Picture picture = pictureRepository.findOne(id);
        PictureDTO pictureDTO = pictureMapper.pictureToPictureDTO(picture);
        return pictureDTO;
    }

    /**
     *  Delete the  picture by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Picture : {}", id);
        pictureRepository.delete(id);
        pictureSearchRepository.delete(id);
    }

    /**
     * Search for the picture corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<PictureDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Pictures for query {}", query);
        Page<Picture> result = pictureSearchRepository.search(queryStringQuery(query), pageable);
        return result.map(picture -> pictureMapper.pictureToPictureDTO(picture));
    }
}
