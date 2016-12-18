package com.accenture.videomanager.repository;

import com.accenture.videomanager.domain.Picture;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Picture entity.
 */
@SuppressWarnings("unused")
public interface PictureRepository extends JpaRepository<Picture,Long> {

}
