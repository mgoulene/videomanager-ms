package com.accenture.videomanager.repository;

import com.accenture.videomanager.domain.Crew;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Crew entity.
 */
@SuppressWarnings("unused")
public interface CrewRepository extends JpaRepository<Crew,Long> {

}
