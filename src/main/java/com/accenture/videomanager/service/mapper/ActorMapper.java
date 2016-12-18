package com.accenture.videomanager.service.mapper;

import com.accenture.videomanager.domain.*;
import com.accenture.videomanager.service.dto.ActorDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity Actor and its DTO ActorDTO.
 */
@Mapper(componentModel = "spring", uses = {PersonMapper.class})
public interface ActorMapper {

    @Mapping(source = "person.id", target = "personId")
    @Mapping(source = "movieActor.id", target = "movieActorId")
    ActorDTO actorToActorDTO(Actor actor);

    List<ActorDTO> actorsToActorDTOs(List<Actor> actors);

    @Mapping(source = "personId", target = "person")
    @Mapping(source = "movieActorId", target = "movieActor")
    Actor actorDTOToActor(ActorDTO actorDTO);

    List<Actor> actorDTOsToActors(List<ActorDTO> actorDTOs);

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
