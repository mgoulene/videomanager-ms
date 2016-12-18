package com.accenture.videomanager.service.mapper;

import com.accenture.videomanager.domain.*;
import com.accenture.videomanager.service.dto.PictureDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity Picture and its DTO PictureDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface PictureMapper {

    @Mapping(source = "movie.id", target = "movieId")
    PictureDTO pictureToPictureDTO(Picture picture);

    List<PictureDTO> picturesToPictureDTOs(List<Picture> pictures);

    @Mapping(source = "movieId", target = "movie")
    Picture pictureDTOToPicture(PictureDTO pictureDTO);

    List<Picture> pictureDTOsToPictures(List<PictureDTO> pictureDTOs);

    default Movie movieFromId(Long id) {
        if (id == null) {
            return null;
        }
        Movie movie = new Movie();
        movie.setId(id);
        return movie;
    }
}
