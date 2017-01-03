package com.accenture.videomanager.repository;

import com.accenture.videomanager.domain.TMDBImporterLog;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the TMDBImporterLog entity.
 */
@SuppressWarnings("unused")
public interface TMDBImporterLogRepository extends JpaRepository<TMDBImporterLog,Long> {

}
