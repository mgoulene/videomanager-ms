package com.accenture.videomanager.service.mapper;

import com.accenture.videomanager.domain.*;
import com.accenture.videomanager.service.dto.GenreDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity Genre and its DTO GenreDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface GenreMapper {

    GenreDTO genreToGenreDTO(Genre genre);

    List<GenreDTO> genresToGenreDTOs(List<Genre> genres);

    @Mapping(target = "movies", ignore = true)
    Genre genreDTOToGenre(GenreDTO genreDTO);

    List<Genre> genreDTOsToGenres(List<GenreDTO> genreDTOs);
}
