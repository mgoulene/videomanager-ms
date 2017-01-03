package com.accenture.videomanager.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.accenture.videomanager.domain.TMDBImporterLog;

import com.accenture.videomanager.repository.TMDBImporterLogRepository;
import com.accenture.videomanager.repository.search.TMDBImporterLogSearchRepository;
import com.accenture.videomanager.web.rest.util.HeaderUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing TMDBImporterLog.
 */
@RestController
@RequestMapping("/api")
public class TMDBImporterLogResource {

    private final Logger log = LoggerFactory.getLogger(TMDBImporterLogResource.class);
        
    @Inject
    private TMDBImporterLogRepository tMDBImporterLogRepository;

    @Inject
    private TMDBImporterLogSearchRepository tMDBImporterLogSearchRepository;

    /**
     * POST  /t-mdb-importer-logs : Create a new tMDBImporterLog.
     *
     * @param tMDBImporterLog the tMDBImporterLog to create
     * @return the ResponseEntity with status 201 (Created) and with body the new tMDBImporterLog, or with status 400 (Bad Request) if the tMDBImporterLog has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/t-mdb-importer-logs")
    @Timed
    public ResponseEntity<TMDBImporterLog> createTMDBImporterLog(@Valid @RequestBody TMDBImporterLog tMDBImporterLog) throws URISyntaxException {
        log.debug("REST request to save TMDBImporterLog : {}", tMDBImporterLog);
        if (tMDBImporterLog.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("tMDBImporterLog", "idexists", "A new tMDBImporterLog cannot already have an ID")).body(null);
        }
        TMDBImporterLog result = tMDBImporterLogRepository.save(tMDBImporterLog);
        tMDBImporterLogSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/t-mdb-importer-logs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("tMDBImporterLog", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /t-mdb-importer-logs : Updates an existing tMDBImporterLog.
     *
     * @param tMDBImporterLog the tMDBImporterLog to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated tMDBImporterLog,
     * or with status 400 (Bad Request) if the tMDBImporterLog is not valid,
     * or with status 500 (Internal Server Error) if the tMDBImporterLog couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/t-mdb-importer-logs")
    @Timed
    public ResponseEntity<TMDBImporterLog> updateTMDBImporterLog(@Valid @RequestBody TMDBImporterLog tMDBImporterLog) throws URISyntaxException {
        log.debug("REST request to update TMDBImporterLog : {}", tMDBImporterLog);
        if (tMDBImporterLog.getId() == null) {
            return createTMDBImporterLog(tMDBImporterLog);
        }
        TMDBImporterLog result = tMDBImporterLogRepository.save(tMDBImporterLog);
        tMDBImporterLogSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("tMDBImporterLog", tMDBImporterLog.getId().toString()))
            .body(result);
    }

    /**
     * GET  /t-mdb-importer-logs : get all the tMDBImporterLogs.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of tMDBImporterLogs in body
     */
    @GetMapping("/t-mdb-importer-logs")
    @Timed
    public List<TMDBImporterLog> getAllTMDBImporterLogs() {
        log.debug("REST request to get all TMDBImporterLogs");
        List<TMDBImporterLog> tMDBImporterLogs = tMDBImporterLogRepository.findAll();
        return tMDBImporterLogs;
    }

    /**
     * GET  /t-mdb-importer-logs/:id : get the "id" tMDBImporterLog.
     *
     * @param id the id of the tMDBImporterLog to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the tMDBImporterLog, or with status 404 (Not Found)
     */
    @GetMapping("/t-mdb-importer-logs/{id}")
    @Timed
    public ResponseEntity<TMDBImporterLog> getTMDBImporterLog(@PathVariable Long id) {
        log.debug("REST request to get TMDBImporterLog : {}", id);
        TMDBImporterLog tMDBImporterLog = tMDBImporterLogRepository.findOne(id);
        return Optional.ofNullable(tMDBImporterLog)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /t-mdb-importer-logs/:id : delete the "id" tMDBImporterLog.
     *
     * @param id the id of the tMDBImporterLog to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/t-mdb-importer-logs/{id}")
    @Timed
    public ResponseEntity<Void> deleteTMDBImporterLog(@PathVariable Long id) {
        log.debug("REST request to delete TMDBImporterLog : {}", id);
        tMDBImporterLogRepository.delete(id);
        tMDBImporterLogSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("tMDBImporterLog", id.toString())).build();
    }

    /**
     * SEARCH  /_search/t-mdb-importer-logs?query=:query : search for the tMDBImporterLog corresponding
     * to the query.
     *
     * @param query the query of the tMDBImporterLog search 
     * @return the result of the search
     */
    @GetMapping("/_search/t-mdb-importer-logs")
    @Timed
    public List<TMDBImporterLog> searchTMDBImporterLogs(@RequestParam String query) {
        log.debug("REST request to search TMDBImporterLogs for query {}", query);
        return StreamSupport
            .stream(tMDBImporterLogSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }


}
