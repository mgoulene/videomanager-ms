package com.accenture.videomanager.repository.search;

import com.accenture.videomanager.domain.TMDBImporterLog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the TMDBImporterLog entity.
 */
public interface TMDBImporterLogSearchRepository extends ElasticsearchRepository<TMDBImporterLog, Long> {
}
