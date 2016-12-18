package com.accenture.videomanager.repository;

import com.accenture.videomanager.domain.Genre;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Genre entity.
 */
@SuppressWarnings("unused")
public interface GenreRepository extends JpaRepository<Genre,Long> {

    Genre findOneByName(String name);

}
