package com.accenture.videomanager.service.mapper;

import com.accenture.videomanager.domain.*;
import com.accenture.videomanager.service.dto.CrewDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity Crew and its DTO CrewDTO.
 */
@Mapper(componentModel = "spring", uses = {PersonMapper.class})
public interface CrewMapper {

    @Mapping(source = "person.id", target = "personId")
    @Mapping(source = "movieCrew.id", target = "movieCrewId")
    CrewDTO crewToCrewDTO(Crew crew);

    List<CrewDTO> crewsToCrewDTOs(List<Crew> crews);

    @Mapping(source = "personId", target = "person")
    @Mapping(source = "movieCrewId", target = "movieCrew")
    Crew crewDTOToCrew(CrewDTO crewDTO);

    List<Crew> crewDTOsToCrews(List<CrewDTO> crewDTOs);

    default Person personFromId(Long id) {
        if (id == null) {
            return null;
        }
        Person person = new Person();
        person.setId(id);
        return person;
    }

    default Movie movieFromId(Long id) {
        if (id == null) {
            return null;
        }
        Movie movie = new Movie();
        movie.setId(id);
        return movie;
    }
}
