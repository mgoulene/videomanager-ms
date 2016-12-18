package com.accenture.videomanager.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Crew.
 */
@Entity
@Table(name = "crew")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "crew")
public class Crew implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "department")
    private String department;

    @Column(name = "job")
    private String job;

    @ManyToOne
    private Person person;

    @ManyToOne
    private Movie movieCrew;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartment() {
        return department;
    }

    public Crew department(String department) {
        this.department = department;
        return this;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getJob() {
        return job;
    }

    public Crew job(String job) {
        this.job = job;
        return this;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public Person getPerson() {
        return person;
    }

    public Crew person(Person person) {
        this.person = person;
        return this;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Movie getMovieCrew() {
        return movieCrew;
    }

    public Crew movieCrew(Movie movie) {
        this.movieCrew = movie;
        return this;
    }

    public void setMovieCrew(Movie movie) {
        this.movieCrew = movie;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Crew crew = (Crew) o;
        if (crew.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, crew.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Crew{" +
            "id=" + id +
            ", department='" + department + "'" +
            ", job='" + job + "'" +
            '}';
    }
}
