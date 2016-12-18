package com.accenture.videomanager.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Actor.
 */
@Entity
@Table(name = "actor")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "actor")
public class Actor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "actor_order", nullable = false)
    private Integer actorOrder;

    @Column(name = "actor_character")
    private String actorCharacter;

    @ManyToOne
    private Person person;

    @ManyToOne
    private Movie movieActor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getActorOrder() {
        return actorOrder;
    }

    public Actor actorOrder(Integer actorOrder) {
        this.actorOrder = actorOrder;
        return this;
    }

    public void setActorOrder(Integer actorOrder) {
        this.actorOrder = actorOrder;
    }

    public String getActorCharacter() {
        return actorCharacter;
    }

    public Actor actorCharacter(String actorCharacter) {
        this.actorCharacter = actorCharacter;
        return this;
    }

    public void setActorCharacter(String actorCharacter) {
        this.actorCharacter = actorCharacter;
    }

    public Person getPerson() {
        return person;
    }

    public Actor person(Person person) {
        this.person = person;
        return this;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Movie getMovieActor() {
        return movieActor;
    }

    public Actor movieActor(Movie movie) {
        this.movieActor = movie;
        return this;
    }

    public void setMovieActor(Movie movie) {
        this.movieActor = movie;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Actor actor = (Actor) o;
        if (actor.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, actor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Actor{" +
            "id=" + id +
            ", actorOrder='" + actorOrder + "'" +
            ", actorCharacter='" + actorCharacter + "'" +
            '}';
    }
}
