package com.accenture.videomanager.web.rest;

import com.accenture.videomanager.service.MovieService;
import com.accenture.videomanager.service.PictureService;
import com.accenture.videomanager.service.TMDBMovieService;
import com.accenture.videomanager.service.dto.MovieDTO;
import com.accenture.videomanager.web.rest.util.HeaderUtil;
import com.codahale.metrics.annotation.Timed;

import info.movito.themoviedbapi.model.MovieDb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api")
public class TMDBMovieResource {
    private static String TMDB_KEY = "c344516cac0ae134d50ea9ed99e6a42c";
    private final Logger log = LoggerFactory.getLogger(TMDBMovieResource.class);
    @Inject
    private MovieService movieService;
    @Inject
    private PictureService pictureService;
    @Inject
    private TMDBMovieService tmdbMovieService;

    @GetMapping("/tmdb-movies/{id}")
    @Timed
    public ResponseEntity<MovieDb> getTMDBMovie(@PathVariable Long id) {
        log.debug("REST request to get TMDBMovie : {}", id);
        MovieDb movieDb = tmdbMovieService.findOne(id.intValue());
        return Optional.ofNullable(movieDb)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/_import/tmdb-movies/{id}")
    @Timed
    public ResponseEntity<MovieDTO> importTMDBMovie(@PathVariable Long id) throws URISyntaxException {
        log.debug("REST request to import TMDBMovie : {}", id);
        MovieDTO createdMovieDTO = tmdbMovieService.saveMovie(id.intValue());
        return ResponseEntity.created(new URI("/api/movies/" + createdMovieDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("movie", createdMovieDTO.getId().toString()))
            .body(createdMovieDTO);

    }

    @PostMapping("/_import/tmdb-movies")
    @Timed
    public void importRangeTMDBMovie(@RequestParam Long fromId, @RequestParam Long toId) throws URISyntaxException {
        log.debug("REST request to import importRangeTMDBMovie : {}", fromId, toId);
        MovieDTO lastCreatedMovieDTO = null;
        ExecutorService executor = Executors.newFixedThreadPool(1);

        for (int i= fromId.intValue(); i<=toId.intValue();i++) {
            //lastCreatedMovieDTO = tmdbMovieService.saveMovie(i);
            Runnable importMovieWorker = new ImportMovieThread(i);
            executor.execute(importMovieWorker);
            /*MovieDTO result = tmdbMovieService.saveMovie(i);
            if (result != null) {
                lastCreatedMovieDTO = result;
            }*/

        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        //MovieDTO lastCreatedMovieDTO = tmdbMovieService.saveTMDBMovies(fromId.intValue(),toId.intValue());
        /*return ResponseEntity.created(new URI("/api/movies/" + lastCreatedMovieDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("movie", lastCreatedMovieDTO.getId().toString()))
            .body(lastCreatedMovieDTO);*/


    }


    /**
     * SEARCH  /_search/movies?query=:query : search for the movie corresponding
     * to the query.
     *
     * @param query the query of the movie search
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/tmdb-movies")
    @Timed
    public List<MovieDb> searchMovies(@RequestParam String query)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Movies for query {}", query);
        List<MovieDb> movieDbs = tmdbMovieService.searchTMDBMovies(query);
        return movieDbs;
    }

    private class ImportMovieThread implements Runnable {
        private int id;
        public ImportMovieThread(int id) {
            this.id = id;
        }
        @Override
        public void run() {
            MovieDTO result = tmdbMovieService.saveMovie(id);
        }
    }
}
