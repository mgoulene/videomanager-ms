package com.accenture.videomanager.web.rest;

import com.accenture.videomanager.VmmsApp;

import com.accenture.videomanager.domain.Crew;
import com.accenture.videomanager.repository.CrewRepository;
import com.accenture.videomanager.service.CrewService;
import com.accenture.videomanager.repository.search.CrewSearchRepository;
import com.accenture.videomanager.service.dto.CrewDTO;
import com.accenture.videomanager.service.mapper.CrewMapper;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the CrewResource REST controller.
 *
 * @see CrewResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = VmmsApp.class)
public class CrewResourceIntTest {

    private static final String DEFAULT_DEPARTMENT = "AAAAAAAAAA";
    private static final String UPDATED_DEPARTMENT = "BBBBBBBBBB";

    private static final String DEFAULT_JOB = "AAAAAAAAAA";
    private static final String UPDATED_JOB = "BBBBBBBBBB";

    @Inject
    private CrewRepository crewRepository;

    @Inject
    private CrewMapper crewMapper;

    @Inject
    private CrewService crewService;

    @Inject
    private CrewSearchRepository crewSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restCrewMockMvc;

    private Crew crew;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        CrewResource crewResource = new CrewResource();
        ReflectionTestUtils.setField(crewResource, "crewService", crewService);
        this.restCrewMockMvc = MockMvcBuilders.standaloneSetup(crewResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Crew createEntity(EntityManager em) {
        Crew crew = new Crew()
                .department(DEFAULT_DEPARTMENT)
                .job(DEFAULT_JOB);
        return crew;
    }

    @Before
    public void initTest() {
        crewSearchRepository.deleteAll();
        crew = createEntity(em);
    }

    @Test
    @Transactional
    public void createCrew() throws Exception {
        int databaseSizeBeforeCreate = crewRepository.findAll().size();

        // Create the Crew
        CrewDTO crewDTO = crewMapper.crewToCrewDTO(crew);

        restCrewMockMvc.perform(post("/api/crews")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(crewDTO)))
            .andExpect(status().isCreated());

        // Validate the Crew in the database
        List<Crew> crewList = crewRepository.findAll();
        assertThat(crewList).hasSize(databaseSizeBeforeCreate + 1);
        Crew testCrew = crewList.get(crewList.size() - 1);
        assertThat(testCrew.getDepartment()).isEqualTo(DEFAULT_DEPARTMENT);
        assertThat(testCrew.getJob()).isEqualTo(DEFAULT_JOB);

        // Validate the Crew in ElasticSearch
        Crew crewEs = crewSearchRepository.findOne(testCrew.getId());
        assertThat(crewEs).isEqualToComparingFieldByField(testCrew);
    }

    @Test
    @Transactional
    public void createCrewWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = crewRepository.findAll().size();

        // Create the Crew with an existing ID
        Crew existingCrew = new Crew();
        existingCrew.setId(1L);
        CrewDTO existingCrewDTO = crewMapper.crewToCrewDTO(existingCrew);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCrewMockMvc.perform(post("/api/crews")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingCrewDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Crew> crewList = crewRepository.findAll();
        assertThat(crewList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllCrews() throws Exception {
        // Initialize the database
        crewRepository.saveAndFlush(crew);

        // Get all the crewList
        restCrewMockMvc.perform(get("/api/crews?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(crew.getId().intValue())))
            .andExpect(jsonPath("$.[*].department").value(hasItem(DEFAULT_DEPARTMENT.toString())))
            .andExpect(jsonPath("$.[*].job").value(hasItem(DEFAULT_JOB.toString())));
    }

    @Test
    @Transactional
    public void getCrew() throws Exception {
        // Initialize the database
        crewRepository.saveAndFlush(crew);

        // Get the crew
        restCrewMockMvc.perform(get("/api/crews/{id}", crew.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(crew.getId().intValue()))
            .andExpect(jsonPath("$.department").value(DEFAULT_DEPARTMENT.toString()))
            .andExpect(jsonPath("$.job").value(DEFAULT_JOB.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingCrew() throws Exception {
        // Get the crew
        restCrewMockMvc.perform(get("/api/crews/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCrew() throws Exception {
        // Initialize the database
        crewRepository.saveAndFlush(crew);
        crewSearchRepository.save(crew);
        int databaseSizeBeforeUpdate = crewRepository.findAll().size();

        // Update the crew
        Crew updatedCrew = crewRepository.findOne(crew.getId());
        updatedCrew
                .department(UPDATED_DEPARTMENT)
                .job(UPDATED_JOB);
        CrewDTO crewDTO = crewMapper.crewToCrewDTO(updatedCrew);

        restCrewMockMvc.perform(put("/api/crews")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(crewDTO)))
            .andExpect(status().isOk());

        // Validate the Crew in the database
        List<Crew> crewList = crewRepository.findAll();
        assertThat(crewList).hasSize(databaseSizeBeforeUpdate);
        Crew testCrew = crewList.get(crewList.size() - 1);
        assertThat(testCrew.getDepartment()).isEqualTo(UPDATED_DEPARTMENT);
        assertThat(testCrew.getJob()).isEqualTo(UPDATED_JOB);

        // Validate the Crew in ElasticSearch
        Crew crewEs = crewSearchRepository.findOne(testCrew.getId());
        assertThat(crewEs).isEqualToComparingFieldByField(testCrew);
    }

    @Test
    @Transactional
    public void updateNonExistingCrew() throws Exception {
        int databaseSizeBeforeUpdate = crewRepository.findAll().size();

        // Create the Crew
        CrewDTO crewDTO = crewMapper.crewToCrewDTO(crew);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restCrewMockMvc.perform(put("/api/crews")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(crewDTO)))
            .andExpect(status().isCreated());

        // Validate the Crew in the database
        List<Crew> crewList = crewRepository.findAll();
        assertThat(crewList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteCrew() throws Exception {
        // Initialize the database
        crewRepository.saveAndFlush(crew);
        crewSearchRepository.save(crew);
        int databaseSizeBeforeDelete = crewRepository.findAll().size();

        // Get the crew
        restCrewMockMvc.perform(delete("/api/crews/{id}", crew.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean crewExistsInEs = crewSearchRepository.exists(crew.getId());
        assertThat(crewExistsInEs).isFalse();

        // Validate the database is empty
        List<Crew> crewList = crewRepository.findAll();
        assertThat(crewList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchCrew() throws Exception {
        // Initialize the database
        crewRepository.saveAndFlush(crew);
        crewSearchRepository.save(crew);

        // Search the crew
        restCrewMockMvc.perform(get("/api/_search/crews?query=id:" + crew.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(crew.getId().intValue())))
            .andExpect(jsonPath("$.[*].department").value(hasItem(DEFAULT_DEPARTMENT.toString())))
            .andExpect(jsonPath("$.[*].job").value(hasItem(DEFAULT_JOB.toString())));
    }
}
