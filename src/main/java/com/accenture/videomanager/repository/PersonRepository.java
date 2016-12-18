package com.accenture.videomanager.repository;

import com.accenture.videomanager.domain.Person;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Person entity.
 */
@SuppressWarnings("unused")
public interface PersonRepository extends JpaRepository<Person,Long> {

    Person findOneByTmdbId(int personTmdbId);

}
