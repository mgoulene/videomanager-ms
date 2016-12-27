package com.accenture.videomanager.service.mapper;

import com.accenture.videomanager.domain.*;
import com.accenture.videomanager.service.dto.MovieDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity Movie and its DTO MovieDTO.
 */
@Mapper(componentModel = "spring", uses = {ActorMapper.class, CrewMapper.class, PictureMapper.class, GenreMapper.class, })
public interface MovieMapper {

    @Mapping(source = "poster.id", target = "posterId")
    @Mapping(source = "backdrop.id", target = "backdropId")
    @Mapping(source = "poster.tmdb_id", target = "posterTmdbId")
    @Mapping(source = "backdrop.tmdb_id", target = "backdropTmdbId")
    MovieDTO movieToMovieDTO(Movie movie);

    List<MovieDTO> moviesToMovieDTOs(List<Movie> movies);

    @Mapping(source = "posterId", target = "poster")
    @Mapping(source = "backdropId", target = "backdrop")
    Movie movieDTOToMovie(MovieDTO movieDTO);

    List<Movie> movieDTOsToMovies(List<MovieDTO> movieDTOs);

    default Picture pictureFromId(Long id) {
        if (id == null) {
            return null;
        }
        Picture picture = new Picture();
        picture.setId(id);
        return picture;
    }

    default Genre genreFromId(Long id) {
        if (id == null) {
            return null;
        }
        Genre genre = new Genre();
        genre.setId(id);
        return genre;
    }
    default Actor actorFromId(Long id) {
        if (id == null) {
            return null;
        }
        Actor actor = new Actor();
        actor.setId(id);
        return actor;
    }

    default Crew crewFromId(Long id) {
        if (id == null) {
            return null;
        }
        Crew crew = new Crew();
        crew.setId(id);
        return crew;
    }

}
