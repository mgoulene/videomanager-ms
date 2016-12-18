package com.accenture.videomanager.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.accenture.videomanager.service.CrewService;
import com.accenture.videomanager.web.rest.util.HeaderUtil;
import com.accenture.videomanager.web.rest.util.PaginationUtil;
import com.accenture.videomanager.service.dto.CrewDTO;

import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Crew.
 */
@RestController
@RequestMapping("/api")
public class CrewResource {

    private final Logger log = LoggerFactory.getLogger(CrewResource.class);
        
    @Inject
    private CrewService crewService;

    /**
     * POST  /crews : Create a new crew.
     *
     * @param crewDTO the crewDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new crewDTO, or with status 400 (Bad Request) if the crew has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/crews")
    @Timed
    public ResponseEntity<CrewDTO> createCrew(@RequestBody CrewDTO crewDTO) throws URISyntaxException {
        log.debug("REST request to save Crew : {}", crewDTO);
        if (crewDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("crew", "idexists", "A new crew cannot already have an ID")).body(null);
        }
        CrewDTO result = crewService.save(crewDTO);
        return ResponseEntity.created(new URI("/api/crews/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("crew", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /crews : Updates an existing crew.
     *
     * @param crewDTO the crewDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated crewDTO,
     * or with status 400 (Bad Request) if the crewDTO is not valid,
     * or with status 500 (Internal Server Error) if the crewDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/crews")
    @Timed
    public ResponseEntity<CrewDTO> updateCrew(@RequestBody CrewDTO crewDTO) throws URISyntaxException {
        log.debug("REST request to update Crew : {}", crewDTO);
        if (crewDTO.getId() == null) {
            return createCrew(crewDTO);
        }
        CrewDTO result = crewService.save(crewDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("crew", crewDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /crews : get all the crews.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of crews in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/crews")
    @Timed
    public ResponseEntity<List<CrewDTO>> getAllCrews(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Crews");
        Page<CrewDTO> page = crewService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/crews");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /crews/:id : get the "id" crew.
     *
     * @param id the id of the crewDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the crewDTO, or with status 404 (Not Found)
     */
    @GetMapping("/crews/{id}")
    @Timed
    public ResponseEntity<CrewDTO> getCrew(@PathVariable Long id) {
        log.debug("REST request to get Crew : {}", id);
        CrewDTO crewDTO = crewService.findOne(id);
        return Optional.ofNullable(crewDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /crews/:id : delete the "id" crew.
     *
     * @param id the id of the crewDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/crews/{id}")
    @Timed
    public ResponseEntity<Void> deleteCrew(@PathVariable Long id) {
        log.debug("REST request to delete Crew : {}", id);
        crewService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("crew", id.toString())).build();
    }

    /**
     * SEARCH  /_search/crews?query=:query : search for the crew corresponding
     * to the query.
     *
     * @param query the query of the crew search 
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/crews")
    @Timed
    public ResponseEntity<List<CrewDTO>> searchCrews(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Crews for query {}", query);
        Page<CrewDTO> page = crewService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/crews");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
