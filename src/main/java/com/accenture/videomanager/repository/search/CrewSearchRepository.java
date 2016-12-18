package com.accenture.videomanager.repository.search;

import com.accenture.videomanager.domain.Crew;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Crew entity.
 */
public interface CrewSearchRepository extends ElasticsearchRepository<Crew, Long> {
}
