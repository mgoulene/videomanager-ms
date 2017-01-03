package com.accenture.videomanager.web.rest;

import com.accenture.videomanager.VmmsApp;

import com.accenture.videomanager.domain.TMDBImporterLog;
import com.accenture.videomanager.repository.TMDBImporterLogRepository;
import com.accenture.videomanager.repository.search.TMDBImporterLogSearchRepository;

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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.List;

import static com.accenture.videomanager.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the TMDBImporterLogResource REST controller.
 *
 * @see TMDBImporterLogResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = VmmsApp.class)
public class TMDBImporterLogResourceIntTest {

    private static final Integer DEFAULT_TMDB_ID = 1;
    private static final Integer UPDATED_TMDB_ID = 2;

    private static final Integer DEFAULT_NUMBER_OF_PEOPLE = 1;
    private static final Integer UPDATED_NUMBER_OF_PEOPLE = 2;

    private static final Integer DEFAULT_NUMBER_OF_IMPORTED_PEOPLE = 1;
    private static final Integer UPDATED_NUMBER_OF_IMPORTED_PEOPLE = 2;

    private static final ZonedDateTime DEFAULT_START_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_START_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final Long DEFAULT_IMPORT_DURATION = 1L;
    private static final Long UPDATED_IMPORT_DURATION = 2L;

    @Inject
    private TMDBImporterLogRepository tMDBImporterLogRepository;

    @Inject
    private TMDBImporterLogSearchRepository tMDBImporterLogSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restTMDBImporterLogMockMvc;

    private TMDBImporterLog tMDBImporterLog;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        TMDBImporterLogResource tMDBImporterLogResource = new TMDBImporterLogResource();
        ReflectionTestUtils.setField(tMDBImporterLogResource, "tMDBImporterLogSearchRepository", tMDBImporterLogSearchRepository);
        ReflectionTestUtils.setField(tMDBImporterLogResource, "tMDBImporterLogRepository", tMDBImporterLogRepository);
        this.restTMDBImporterLogMockMvc = MockMvcBuilders.standaloneSetup(tMDBImporterLogResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TMDBImporterLog createEntity(EntityManager em) {
        TMDBImporterLog tMDBImporterLog = new TMDBImporterLog()
                .tmdbId(DEFAULT_TMDB_ID)
                .numberOfPeople(DEFAULT_NUMBER_OF_PEOPLE)
                .numberOfImportedPeople(DEFAULT_NUMBER_OF_IMPORTED_PEOPLE)
                .startTime(DEFAULT_START_TIME)
                .importDuration(DEFAULT_IMPORT_DURATION);
        return tMDBImporterLog;
    }

    @Before
    public void initTest() {
        tMDBImporterLogSearchRepository.deleteAll();
        tMDBImporterLog = createEntity(em);
    }

    @Test
    @Transactional
    public void createTMDBImporterLog() throws Exception {
        int databaseSizeBeforeCreate = tMDBImporterLogRepository.findAll().size();

        // Create the TMDBImporterLog

        restTMDBImporterLogMockMvc.perform(post("/api/t-mdb-importer-logs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(tMDBImporterLog)))
            .andExpect(status().isCreated());

        // Validate the TMDBImporterLog in the database
        List<TMDBImporterLog> tMDBImporterLogList = tMDBImporterLogRepository.findAll();
        assertThat(tMDBImporterLogList).hasSize(databaseSizeBeforeCreate + 1);
        TMDBImporterLog testTMDBImporterLog = tMDBImporterLogList.get(tMDBImporterLogList.size() - 1);
        assertThat(testTMDBImporterLog.getTmdbId()).isEqualTo(DEFAULT_TMDB_ID);
        assertThat(testTMDBImporterLog.getNumberOfPeople()).isEqualTo(DEFAULT_NUMBER_OF_PEOPLE);
        assertThat(testTMDBImporterLog.getNumberOfImportedPeople()).isEqualTo(DEFAULT_NUMBER_OF_IMPORTED_PEOPLE);
        assertThat(testTMDBImporterLog.getStartTime()).isEqualTo(DEFAULT_START_TIME);
        assertThat(testTMDBImporterLog.getImportDuration()).isEqualTo(DEFAULT_IMPORT_DURATION);

        // Validate the TMDBImporterLog in ElasticSearch
        TMDBImporterLog tMDBImporterLogEs = tMDBImporterLogSearchRepository.findOne(testTMDBImporterLog.getId());
        assertThat(tMDBImporterLogEs).isEqualToComparingFieldByField(testTMDBImporterLog);
    }

    @Test
    @Transactional
    public void createTMDBImporterLogWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = tMDBImporterLogRepository.findAll().size();

        // Create the TMDBImporterLog with an existing ID
        TMDBImporterLog existingTMDBImporterLog = new TMDBImporterLog();
        existingTMDBImporterLog.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restTMDBImporterLogMockMvc.perform(post("/api/t-mdb-importer-logs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingTMDBImporterLog)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<TMDBImporterLog> tMDBImporterLogList = tMDBImporterLogRepository.findAll();
        assertThat(tMDBImporterLogList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkTmdbIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = tMDBImporterLogRepository.findAll().size();
        // set the field null
        tMDBImporterLog.setTmdbId(null);

        // Create the TMDBImporterLog, which fails.

        restTMDBImporterLogMockMvc.perform(post("/api/t-mdb-importer-logs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(tMDBImporterLog)))
            .andExpect(status().isBadRequest());

        List<TMDBImporterLog> tMDBImporterLogList = tMDBImporterLogRepository.findAll();
        assertThat(tMDBImporterLogList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkNumberOfPeopleIsRequired() throws Exception {
        int databaseSizeBeforeTest = tMDBImporterLogRepository.findAll().size();
        // set the field null
        tMDBImporterLog.setNumberOfPeople(null);

        // Create the TMDBImporterLog, which fails.

        restTMDBImporterLogMockMvc.perform(post("/api/t-mdb-importer-logs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(tMDBImporterLog)))
            .andExpect(status().isBadRequest());

        List<TMDBImporterLog> tMDBImporterLogList = tMDBImporterLogRepository.findAll();
        assertThat(tMDBImporterLogList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkNumberOfImportedPeopleIsRequired() throws Exception {
        int databaseSizeBeforeTest = tMDBImporterLogRepository.findAll().size();
        // set the field null
        tMDBImporterLog.setNumberOfImportedPeople(null);

        // Create the TMDBImporterLog, which fails.

        restTMDBImporterLogMockMvc.perform(post("/api/t-mdb-importer-logs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(tMDBImporterLog)))
            .andExpect(status().isBadRequest());

        List<TMDBImporterLog> tMDBImporterLogList = tMDBImporterLogRepository.findAll();
        assertThat(tMDBImporterLogList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStartTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = tMDBImporterLogRepository.findAll().size();
        // set the field null
        tMDBImporterLog.setStartTime(null);

        // Create the TMDBImporterLog, which fails.

        restTMDBImporterLogMockMvc.perform(post("/api/t-mdb-importer-logs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(tMDBImporterLog)))
            .andExpect(status().isBadRequest());

        List<TMDBImporterLog> tMDBImporterLogList = tMDBImporterLogRepository.findAll();
        assertThat(tMDBImporterLogList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkImportDurationIsRequired() throws Exception {
        int databaseSizeBeforeTest = tMDBImporterLogRepository.findAll().size();
        // set the field null
        tMDBImporterLog.setImportDuration(null);

        // Create the TMDBImporterLog, which fails.

        restTMDBImporterLogMockMvc.perform(post("/api/t-mdb-importer-logs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(tMDBImporterLog)))
            .andExpect(status().isBadRequest());

        List<TMDBImporterLog> tMDBImporterLogList = tMDBImporterLogRepository.findAll();
        assertThat(tMDBImporterLogList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllTMDBImporterLogs() throws Exception {
        // Initialize the database
        tMDBImporterLogRepository.saveAndFlush(tMDBImporterLog);

        // Get all the tMDBImporterLogList
        restTMDBImporterLogMockMvc.perform(get("/api/t-mdb-importer-logs?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tMDBImporterLog.getId().intValue())))
            .andExpect(jsonPath("$.[*].tmdbId").value(hasItem(DEFAULT_TMDB_ID)))
            .andExpect(jsonPath("$.[*].numberOfPeople").value(hasItem(DEFAULT_NUMBER_OF_PEOPLE)))
            .andExpect(jsonPath("$.[*].numberOfImportedPeople").value(hasItem(DEFAULT_NUMBER_OF_IMPORTED_PEOPLE)))
            .andExpect(jsonPath("$.[*].startTime").value(hasItem(sameInstant(DEFAULT_START_TIME))))
            .andExpect(jsonPath("$.[*].importDuration").value(hasItem(DEFAULT_IMPORT_DURATION.intValue())));
    }

    @Test
    @Transactional
    public void getTMDBImporterLog() throws Exception {
        // Initialize the database
        tMDBImporterLogRepository.saveAndFlush(tMDBImporterLog);

        // Get the tMDBImporterLog
        restTMDBImporterLogMockMvc.perform(get("/api/t-mdb-importer-logs/{id}", tMDBImporterLog.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(tMDBImporterLog.getId().intValue()))
            .andExpect(jsonPath("$.tmdbId").value(DEFAULT_TMDB_ID))
            .andExpect(jsonPath("$.numberOfPeople").value(DEFAULT_NUMBER_OF_PEOPLE))
            .andExpect(jsonPath("$.numberOfImportedPeople").value(DEFAULT_NUMBER_OF_IMPORTED_PEOPLE))
            .andExpect(jsonPath("$.startTime").value(sameInstant(DEFAULT_START_TIME)))
            .andExpect(jsonPath("$.importDuration").value(DEFAULT_IMPORT_DURATION.intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingTMDBImporterLog() throws Exception {
        // Get the tMDBImporterLog
        restTMDBImporterLogMockMvc.perform(get("/api/t-mdb-importer-logs/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateTMDBImporterLog() throws Exception {
        // Initialize the database
        tMDBImporterLogRepository.saveAndFlush(tMDBImporterLog);
        tMDBImporterLogSearchRepository.save(tMDBImporterLog);
        int databaseSizeBeforeUpdate = tMDBImporterLogRepository.findAll().size();

        // Update the tMDBImporterLog
        TMDBImporterLog updatedTMDBImporterLog = tMDBImporterLogRepository.findOne(tMDBImporterLog.getId());
        updatedTMDBImporterLog
                .tmdbId(UPDATED_TMDB_ID)
                .numberOfPeople(UPDATED_NUMBER_OF_PEOPLE)
                .numberOfImportedPeople(UPDATED_NUMBER_OF_IMPORTED_PEOPLE)
                .startTime(UPDATED_START_TIME)
                .importDuration(UPDATED_IMPORT_DURATION);

        restTMDBImporterLogMockMvc.perform(put("/api/t-mdb-importer-logs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedTMDBImporterLog)))
            .andExpect(status().isOk());

        // Validate the TMDBImporterLog in the database
        List<TMDBImporterLog> tMDBImporterLogList = tMDBImporterLogRepository.findAll();
        assertThat(tMDBImporterLogList).hasSize(databaseSizeBeforeUpdate);
        TMDBImporterLog testTMDBImporterLog = tMDBImporterLogList.get(tMDBImporterLogList.size() - 1);
        assertThat(testTMDBImporterLog.getTmdbId()).isEqualTo(UPDATED_TMDB_ID);
        assertThat(testTMDBImporterLog.getNumberOfPeople()).isEqualTo(UPDATED_NUMBER_OF_PEOPLE);
        assertThat(testTMDBImporterLog.getNumberOfImportedPeople()).isEqualTo(UPDATED_NUMBER_OF_IMPORTED_PEOPLE);
        assertThat(testTMDBImporterLog.getStartTime()).isEqualTo(UPDATED_START_TIME);
        assertThat(testTMDBImporterLog.getImportDuration()).isEqualTo(UPDATED_IMPORT_DURATION);

        // Validate the TMDBImporterLog in ElasticSearch
        TMDBImporterLog tMDBImporterLogEs = tMDBImporterLogSearchRepository.findOne(testTMDBImporterLog.getId());
        assertThat(tMDBImporterLogEs).isEqualToComparingFieldByField(testTMDBImporterLog);
    }

    @Test
    @Transactional
    public void updateNonExistingTMDBImporterLog() throws Exception {
        int databaseSizeBeforeUpdate = tMDBImporterLogRepository.findAll().size();

        // Create the TMDBImporterLog

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restTMDBImporterLogMockMvc.perform(put("/api/t-mdb-importer-logs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(tMDBImporterLog)))
            .andExpect(status().isCreated());

        // Validate the TMDBImporterLog in the database
        List<TMDBImporterLog> tMDBImporterLogList = tMDBImporterLogRepository.findAll();
        assertThat(tMDBImporterLogList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteTMDBImporterLog() throws Exception {
        // Initialize the database
        tMDBImporterLogRepository.saveAndFlush(tMDBImporterLog);
        tMDBImporterLogSearchRepository.save(tMDBImporterLog);
        int databaseSizeBeforeDelete = tMDBImporterLogRepository.findAll().size();

        // Get the tMDBImporterLog
        restTMDBImporterLogMockMvc.perform(delete("/api/t-mdb-importer-logs/{id}", tMDBImporterLog.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean tMDBImporterLogExistsInEs = tMDBImporterLogSearchRepository.exists(tMDBImporterLog.getId());
        assertThat(tMDBImporterLogExistsInEs).isFalse();

        // Validate the database is empty
        List<TMDBImporterLog> tMDBImporterLogList = tMDBImporterLogRepository.findAll();
        assertThat(tMDBImporterLogList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchTMDBImporterLog() throws Exception {
        // Initialize the database
        tMDBImporterLogRepository.saveAndFlush(tMDBImporterLog);
        tMDBImporterLogSearchRepository.save(tMDBImporterLog);

        // Search the tMDBImporterLog
        restTMDBImporterLogMockMvc.perform(get("/api/_search/t-mdb-importer-logs?query=id:" + tMDBImporterLog.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tMDBImporterLog.getId().intValue())))
            .andExpect(jsonPath("$.[*].tmdbId").value(hasItem(DEFAULT_TMDB_ID)))
            .andExpect(jsonPath("$.[*].numberOfPeople").value(hasItem(DEFAULT_NUMBER_OF_PEOPLE)))
            .andExpect(jsonPath("$.[*].numberOfImportedPeople").value(hasItem(DEFAULT_NUMBER_OF_IMPORTED_PEOPLE)))
            .andExpect(jsonPath("$.[*].startTime").value(hasItem(sameInstant(DEFAULT_START_TIME))))
            .andExpect(jsonPath("$.[*].importDuration").value(hasItem(DEFAULT_IMPORT_DURATION.intValue())));
    }
}
