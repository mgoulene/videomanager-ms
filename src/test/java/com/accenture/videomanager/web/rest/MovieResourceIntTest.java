package com.accenture.videomanager.web.rest;

import com.accenture.videomanager.VmmsApp;

import com.accenture.videomanager.domain.Movie;
import com.accenture.videomanager.repository.MovieRepository;
import com.accenture.videomanager.service.MovieService;
import com.accenture.videomanager.repository.search.MovieSearchRepository;
import com.accenture.videomanager.service.dto.MovieDTO;
import com.accenture.videomanager.service.mapper.MovieMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the MovieResource REST controller.
 *
 * @see MovieResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = VmmsApp.class)
public class MovieResourceIntTest {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_ORIGINAL_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_ORIGINAL_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_OVERVIEW = "AAAAAAAAAA";
    private static final String UPDATED_OVERVIEW = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_RELEASE_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_RELEASE_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final Integer DEFAULT_RUNTIME = 1;
    private static final Integer UPDATED_RUNTIME = 2;

    private static final Float DEFAULT_VOTE_RATING = 0F;
    private static final Float UPDATED_VOTE_RATING = 1F;

    private static final Integer DEFAULT_VOTE_COUNT = 1;
    private static final Integer UPDATED_VOTE_COUNT = 2;

    private static final String DEFAULT_HOMEPAGE = "AAAAAAAAAA";
    private static final String UPDATED_HOMEPAGE = "BBBBBBBBBB";

    private static final Long DEFAULT_BUDGET = 1L;
    private static final Long UPDATED_BUDGET = 2L;

    private static final Long DEFAULT_REVENUE = 1L;
    private static final Long UPDATED_REVENUE = 2L;

    private static final Integer DEFAULT_TMDB_ID = 1;
    private static final Integer UPDATED_TMDB_ID = 2;

    @Inject
    private MovieRepository movieRepository;

    @Inject
    private MovieMapper movieMapper;

    @Inject
    private MovieService movieService;

    @Inject
    private MovieSearchRepository movieSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restMovieMockMvc;

    private Movie movie;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        MovieResource movieResource = new MovieResource();
        ReflectionTestUtils.setField(movieResource, "movieService", movieService);
        this.restMovieMockMvc = MockMvcBuilders.standaloneSetup(movieResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Movie createEntity(EntityManager em) {
        Movie movie = new Movie()
                .title(DEFAULT_TITLE)
                .originalTitle(DEFAULT_ORIGINAL_TITLE)
                .overview(DEFAULT_OVERVIEW)
                .releaseDate(DEFAULT_RELEASE_DATE)
                .runtime(DEFAULT_RUNTIME)
                .voteRating(DEFAULT_VOTE_RATING)
                .voteCount(DEFAULT_VOTE_COUNT)
                .homepage(DEFAULT_HOMEPAGE)
                .budget(DEFAULT_BUDGET)
                .revenue(DEFAULT_REVENUE)
                .tmdbId(DEFAULT_TMDB_ID);
        return movie;
    }

    @Before
    public void initTest() {
        movieSearchRepository.deleteAll();
        movie = createEntity(em);
    }

    @Test
    @Transactional
    public void createMovie() throws Exception {
        int databaseSizeBeforeCreate = movieRepository.findAll().size();

        // Create the Movie
        MovieDTO movieDTO = movieMapper.movieToMovieDTO(movie);

        restMovieMockMvc.perform(post("/api/movies")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(movieDTO)))
            .andExpect(status().isCreated());

        // Validate the Movie in the database
        List<Movie> movieList = movieRepository.findAll();
        assertThat(movieList).hasSize(databaseSizeBeforeCreate + 1);
        Movie testMovie = movieList.get(movieList.size() - 1);
        assertThat(testMovie.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testMovie.getOriginalTitle()).isEqualTo(DEFAULT_ORIGINAL_TITLE);
        assertThat(testMovie.getOverview()).isEqualTo(DEFAULT_OVERVIEW);
        assertThat(testMovie.getReleaseDate()).isEqualTo(DEFAULT_RELEASE_DATE);
        assertThat(testMovie.getRuntime()).isEqualTo(DEFAULT_RUNTIME);
        assertThat(testMovie.getVoteRating()).isEqualTo(DEFAULT_VOTE_RATING);
        assertThat(testMovie.getVoteCount()).isEqualTo(DEFAULT_VOTE_COUNT);
        assertThat(testMovie.getHomepage()).isEqualTo(DEFAULT_HOMEPAGE);
        assertThat(testMovie.getBudget()).isEqualTo(DEFAULT_BUDGET);
        assertThat(testMovie.getRevenue()).isEqualTo(DEFAULT_REVENUE);
        assertThat(testMovie.getTmdbId()).isEqualTo(DEFAULT_TMDB_ID);

        // Validate the Movie in ElasticSearch
        Movie movieEs = movieSearchRepository.findOne(testMovie.getId());
        assertThat(movieEs).isEqualToComparingFieldByField(testMovie);
    }

    @Test
    @Transactional
    public void createMovieWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = movieRepository.findAll().size();

        // Create the Movie with an existing ID
        Movie existingMovie = new Movie();
        existingMovie.setId(1L);
        MovieDTO existingMovieDTO = movieMapper.movieToMovieDTO(existingMovie);

        // An entity with an existing ID cannot be created, so this API call must fail
        restMovieMockMvc.perform(post("/api/movies")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingMovieDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Movie> movieList = movieRepository.findAll();
        assertThat(movieList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = movieRepository.findAll().size();
        // set the field null
        movie.setTitle(null);

        // Create the Movie, which fails.
        MovieDTO movieDTO = movieMapper.movieToMovieDTO(movie);

        restMovieMockMvc.perform(post("/api/movies")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(movieDTO)))
            .andExpect(status().isBadRequest());

        List<Movie> movieList = movieRepository.findAll();
        assertThat(movieList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllMovies() throws Exception {
        // Initialize the database
        movieRepository.saveAndFlush(movie);

        // Get all the movieList
        restMovieMockMvc.perform(get("/api/movies?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(movie.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].originalTitle").value(hasItem(DEFAULT_ORIGINAL_TITLE.toString())))
            .andExpect(jsonPath("$.[*].overview").value(hasItem(DEFAULT_OVERVIEW.toString())))
            .andExpect(jsonPath("$.[*].releaseDate").value(hasItem(DEFAULT_RELEASE_DATE.toString())))
            .andExpect(jsonPath("$.[*].runtime").value(hasItem(DEFAULT_RUNTIME)))
            .andExpect(jsonPath("$.[*].voteRating").value(hasItem(DEFAULT_VOTE_RATING.doubleValue())))
            .andExpect(jsonPath("$.[*].voteCount").value(hasItem(DEFAULT_VOTE_COUNT)))
            .andExpect(jsonPath("$.[*].homepage").value(hasItem(DEFAULT_HOMEPAGE.toString())))
            .andExpect(jsonPath("$.[*].budget").value(hasItem(DEFAULT_BUDGET.intValue())))
            .andExpect(jsonPath("$.[*].revenue").value(hasItem(DEFAULT_REVENUE.intValue())))
            .andExpect(jsonPath("$.[*].tmdbId").value(hasItem(DEFAULT_TMDB_ID)));
    }

    @Test
    @Transactional
    public void getMovie() throws Exception {
        // Initialize the database
        movieRepository.saveAndFlush(movie);

        // Get the movie
        restMovieMockMvc.perform(get("/api/movies/{id}", movie.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(movie.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.originalTitle").value(DEFAULT_ORIGINAL_TITLE.toString()))
            .andExpect(jsonPath("$.overview").value(DEFAULT_OVERVIEW.toString()))
            .andExpect(jsonPath("$.releaseDate").value(DEFAULT_RELEASE_DATE.toString()))
            .andExpect(jsonPath("$.runtime").value(DEFAULT_RUNTIME))
            .andExpect(jsonPath("$.voteRating").value(DEFAULT_VOTE_RATING.doubleValue()))
            .andExpect(jsonPath("$.voteCount").value(DEFAULT_VOTE_COUNT))
            .andExpect(jsonPath("$.homepage").value(DEFAULT_HOMEPAGE.toString()))
            .andExpect(jsonPath("$.budget").value(DEFAULT_BUDGET.intValue()))
            .andExpect(jsonPath("$.revenue").value(DEFAULT_REVENUE.intValue()))
            .andExpect(jsonPath("$.tmdbId").value(DEFAULT_TMDB_ID));
    }

    @Test
    @Transactional
    public void getNonExistingMovie() throws Exception {
        // Get the movie
        restMovieMockMvc.perform(get("/api/movies/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateMovie() throws Exception {
        // Initialize the database
        movieRepository.saveAndFlush(movie);
        movieSearchRepository.save(movie);
        int databaseSizeBeforeUpdate = movieRepository.findAll().size();

        // Update the movie
        Movie updatedMovie = movieRepository.findOne(movie.getId());
        updatedMovie
                .title(UPDATED_TITLE)
                .originalTitle(UPDATED_ORIGINAL_TITLE)
                .overview(UPDATED_OVERVIEW)
                .releaseDate(UPDATED_RELEASE_DATE)
                .runtime(UPDATED_RUNTIME)
                .voteRating(UPDATED_VOTE_RATING)
                .voteCount(UPDATED_VOTE_COUNT)
                .homepage(UPDATED_HOMEPAGE)
                .budget(UPDATED_BUDGET)
                .revenue(UPDATED_REVENUE)
                .tmdbId(UPDATED_TMDB_ID);
        MovieDTO movieDTO = movieMapper.movieToMovieDTO(updatedMovie);

        restMovieMockMvc.perform(put("/api/movies")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(movieDTO)))
            .andExpect(status().isOk());

        // Validate the Movie in the database
        List<Movie> movieList = movieRepository.findAll();
        assertThat(movieList).hasSize(databaseSizeBeforeUpdate);
        Movie testMovie = movieList.get(movieList.size() - 1);
        assertThat(testMovie.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testMovie.getOriginalTitle()).isEqualTo(UPDATED_ORIGINAL_TITLE);
        assertThat(testMovie.getOverview()).isEqualTo(UPDATED_OVERVIEW);
        assertThat(testMovie.getReleaseDate()).isEqualTo(UPDATED_RELEASE_DATE);
        assertThat(testMovie.getRuntime()).isEqualTo(UPDATED_RUNTIME);
        assertThat(testMovie.getVoteRating()).isEqualTo(UPDATED_VOTE_RATING);
        assertThat(testMovie.getVoteCount()).isEqualTo(UPDATED_VOTE_COUNT);
        assertThat(testMovie.getHomepage()).isEqualTo(UPDATED_HOMEPAGE);
        assertThat(testMovie.getBudget()).isEqualTo(UPDATED_BUDGET);
        assertThat(testMovie.getRevenue()).isEqualTo(UPDATED_REVENUE);
        assertThat(testMovie.getTmdbId()).isEqualTo(UPDATED_TMDB_ID);

        // Validate the Movie in ElasticSearch
        Movie movieEs = movieSearchRepository.findOne(testMovie.getId());
        assertThat(movieEs).isEqualToComparingFieldByField(testMovie);
    }

    @Test
    @Transactional
    public void updateNonExistingMovie() throws Exception {
        int databaseSizeBeforeUpdate = movieRepository.findAll().size();

        // Create the Movie
        MovieDTO movieDTO = movieMapper.movieToMovieDTO(movie);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restMovieMockMvc.perform(put("/api/movies")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(movieDTO)))
            .andExpect(status().isCreated());

        // Validate the Movie in the database
        List<Movie> movieList = movieRepository.findAll();
        assertThat(movieList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteMovie() throws Exception {
        // Initialize the database
        movieRepository.saveAndFlush(movie);
        movieSearchRepository.save(movie);
        int databaseSizeBeforeDelete = movieRepository.findAll().size();

        // Get the movie
        restMovieMockMvc.perform(delete("/api/movies/{id}", movie.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean movieExistsInEs = movieSearchRepository.exists(movie.getId());
        assertThat(movieExistsInEs).isFalse();

        // Validate the database is empty
        List<Movie> movieList = movieRepository.findAll();
        assertThat(movieList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchMovie() throws Exception {
        // Initialize the database
        movieRepository.saveAndFlush(movie);
        movieSearchRepository.save(movie);

        // Search the movie
        restMovieMockMvc.perform(get("/api/_search/movies?query=id:" + movie.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(movie.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].originalTitle").value(hasItem(DEFAULT_ORIGINAL_TITLE.toString())))
            .andExpect(jsonPath("$.[*].overview").value(hasItem(DEFAULT_OVERVIEW.toString())))
            .andExpect(jsonPath("$.[*].releaseDate").value(hasItem(DEFAULT_RELEASE_DATE.toString())))
            .andExpect(jsonPath("$.[*].runtime").value(hasItem(DEFAULT_RUNTIME)))
            .andExpect(jsonPath("$.[*].voteRating").value(hasItem(DEFAULT_VOTE_RATING.doubleValue())))
            .andExpect(jsonPath("$.[*].voteCount").value(hasItem(DEFAULT_VOTE_COUNT)))
            .andExpect(jsonPath("$.[*].homepage").value(hasItem(DEFAULT_HOMEPAGE.toString())))
            .andExpect(jsonPath("$.[*].budget").value(hasItem(DEFAULT_BUDGET.intValue())))
            .andExpect(jsonPath("$.[*].revenue").value(hasItem(DEFAULT_REVENUE.intValue())))
            .andExpect(jsonPath("$.[*].tmdbId").value(hasItem(DEFAULT_TMDB_ID)));
    }
}
