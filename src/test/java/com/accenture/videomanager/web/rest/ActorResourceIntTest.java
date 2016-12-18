package com.accenture.videomanager.web.rest;

import com.accenture.videomanager.VmmsApp;

import com.accenture.videomanager.domain.Actor;
import com.accenture.videomanager.repository.ActorRepository;
import com.accenture.videomanager.service.ActorService;
import com.accenture.videomanager.repository.search.ActorSearchRepository;
import com.accenture.videomanager.service.dto.ActorDTO;
import com.accenture.videomanager.service.mapper.ActorMapper;

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
 * Test class for the ActorResource REST controller.
 *
 * @see ActorResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = VmmsApp.class)
public class ActorResourceIntTest {

    private static final Integer DEFAULT_ACTOR_ORDER = 1;
    private static final Integer UPDATED_ACTOR_ORDER = 2;

    private static final String DEFAULT_ACTOR_CHARACTER = "AAAAAAAAAA";
    private static final String UPDATED_ACTOR_CHARACTER = "BBBBBBBBBB";

    @Inject
    private ActorRepository actorRepository;

    @Inject
    private ActorMapper actorMapper;

    @Inject
    private ActorService actorService;

    @Inject
    private ActorSearchRepository actorSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restActorMockMvc;

    private Actor actor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ActorResource actorResource = new ActorResource();
        ReflectionTestUtils.setField(actorResource, "actorService", actorService);
        this.restActorMockMvc = MockMvcBuilders.standaloneSetup(actorResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Actor createEntity(EntityManager em) {
        Actor actor = new Actor()
                .actorOrder(DEFAULT_ACTOR_ORDER)
                .actorCharacter(DEFAULT_ACTOR_CHARACTER);
        return actor;
    }

    @Before
    public void initTest() {
        actorSearchRepository.deleteAll();
        actor = createEntity(em);
    }

    @Test
    @Transactional
    public void createActor() throws Exception {
        int databaseSizeBeforeCreate = actorRepository.findAll().size();

        // Create the Actor
        ActorDTO actorDTO = actorMapper.actorToActorDTO(actor);

        restActorMockMvc.perform(post("/api/actors")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(actorDTO)))
            .andExpect(status().isCreated());

        // Validate the Actor in the database
        List<Actor> actorList = actorRepository.findAll();
        assertThat(actorList).hasSize(databaseSizeBeforeCreate + 1);
        Actor testActor = actorList.get(actorList.size() - 1);
        assertThat(testActor.getActorOrder()).isEqualTo(DEFAULT_ACTOR_ORDER);
        assertThat(testActor.getActorCharacter()).isEqualTo(DEFAULT_ACTOR_CHARACTER);

        // Validate the Actor in ElasticSearch
        Actor actorEs = actorSearchRepository.findOne(testActor.getId());
        assertThat(actorEs).isEqualToComparingFieldByField(testActor);
    }

    @Test
    @Transactional
    public void createActorWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = actorRepository.findAll().size();

        // Create the Actor with an existing ID
        Actor existingActor = new Actor();
        existingActor.setId(1L);
        ActorDTO existingActorDTO = actorMapper.actorToActorDTO(existingActor);

        // An entity with an existing ID cannot be created, so this API call must fail
        restActorMockMvc.perform(post("/api/actors")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingActorDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Actor> actorList = actorRepository.findAll();
        assertThat(actorList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkActorOrderIsRequired() throws Exception {
        int databaseSizeBeforeTest = actorRepository.findAll().size();
        // set the field null
        actor.setActorOrder(null);

        // Create the Actor, which fails.
        ActorDTO actorDTO = actorMapper.actorToActorDTO(actor);

        restActorMockMvc.perform(post("/api/actors")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(actorDTO)))
            .andExpect(status().isBadRequest());

        List<Actor> actorList = actorRepository.findAll();
        assertThat(actorList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllActors() throws Exception {
        // Initialize the database
        actorRepository.saveAndFlush(actor);

        // Get all the actorList
        restActorMockMvc.perform(get("/api/actors?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(actor.getId().intValue())))
            .andExpect(jsonPath("$.[*].actorOrder").value(hasItem(DEFAULT_ACTOR_ORDER)))
            .andExpect(jsonPath("$.[*].actorCharacter").value(hasItem(DEFAULT_ACTOR_CHARACTER.toString())));
    }

    @Test
    @Transactional
    public void getActor() throws Exception {
        // Initialize the database
        actorRepository.saveAndFlush(actor);

        // Get the actor
        restActorMockMvc.perform(get("/api/actors/{id}", actor.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(actor.getId().intValue()))
            .andExpect(jsonPath("$.actorOrder").value(DEFAULT_ACTOR_ORDER))
            .andExpect(jsonPath("$.actorCharacter").value(DEFAULT_ACTOR_CHARACTER.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingActor() throws Exception {
        // Get the actor
        restActorMockMvc.perform(get("/api/actors/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateActor() throws Exception {
        // Initialize the database
        actorRepository.saveAndFlush(actor);
        actorSearchRepository.save(actor);
        int databaseSizeBeforeUpdate = actorRepository.findAll().size();

        // Update the actor
        Actor updatedActor = actorRepository.findOne(actor.getId());
        updatedActor
                .actorOrder(UPDATED_ACTOR_ORDER)
                .actorCharacter(UPDATED_ACTOR_CHARACTER);
        ActorDTO actorDTO = actorMapper.actorToActorDTO(updatedActor);

        restActorMockMvc.perform(put("/api/actors")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(actorDTO)))
            .andExpect(status().isOk());

        // Validate the Actor in the database
        List<Actor> actorList = actorRepository.findAll();
        assertThat(actorList).hasSize(databaseSizeBeforeUpdate);
        Actor testActor = actorList.get(actorList.size() - 1);
        assertThat(testActor.getActorOrder()).isEqualTo(UPDATED_ACTOR_ORDER);
        assertThat(testActor.getActorCharacter()).isEqualTo(UPDATED_ACTOR_CHARACTER);

        // Validate the Actor in ElasticSearch
        Actor actorEs = actorSearchRepository.findOne(testActor.getId());
        assertThat(actorEs).isEqualToComparingFieldByField(testActor);
    }

    @Test
    @Transactional
    public void updateNonExistingActor() throws Exception {
        int databaseSizeBeforeUpdate = actorRepository.findAll().size();

        // Create the Actor
        ActorDTO actorDTO = actorMapper.actorToActorDTO(actor);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restActorMockMvc.perform(put("/api/actors")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(actorDTO)))
            .andExpect(status().isCreated());

        // Validate the Actor in the database
        List<Actor> actorList = actorRepository.findAll();
        assertThat(actorList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteActor() throws Exception {
        // Initialize the database
        actorRepository.saveAndFlush(actor);
        actorSearchRepository.save(actor);
        int databaseSizeBeforeDelete = actorRepository.findAll().size();

        // Get the actor
        restActorMockMvc.perform(delete("/api/actors/{id}", actor.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean actorExistsInEs = actorSearchRepository.exists(actor.getId());
        assertThat(actorExistsInEs).isFalse();

        // Validate the database is empty
        List<Actor> actorList = actorRepository.findAll();
        assertThat(actorList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchActor() throws Exception {
        // Initialize the database
        actorRepository.saveAndFlush(actor);
        actorSearchRepository.save(actor);

        // Search the actor
        restActorMockMvc.perform(get("/api/_search/actors?query=id:" + actor.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(actor.getId().intValue())))
            .andExpect(jsonPath("$.[*].actorOrder").value(hasItem(DEFAULT_ACTOR_ORDER)))
            .andExpect(jsonPath("$.[*].actorCharacter").value(hasItem(DEFAULT_ACTOR_CHARACTER.toString())));
    }
}
