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

    PictureDTO pictureToPictureDTO(Picture picture);

    List<PictureDTO> picturesToPictureDTOs(List<Picture> pictures);

    Picture pictureDTOToPicture(PictureDTO pictureDTO);

    List<Picture> pictureDTOsToPictures(List<PictureDTO> pictureDTOs);
}
