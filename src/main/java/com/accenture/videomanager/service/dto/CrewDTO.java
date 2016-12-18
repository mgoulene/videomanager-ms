package com.accenture.videomanager.service.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;


/**
 * A DTO for the Crew entity.
 */
public class CrewDTO implements Serializable {

    private Long id;

    private String department;

    private String job;


    private Long personId;

    private PersonDTO person;

    private Long movieCrewId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Long getMovieCrewId() {
        return movieCrewId;
    }

    public void setMovieCrewId(Long movieId) {
        this.movieCrewId = movieId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CrewDTO crewDTO = (CrewDTO) o;

        if ( ! Objects.equals(id, crewDTO.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "CrewDTO{" +
            "id=" + id +
            ", department='" + department + "'" +
            ", job='" + job + "'" +
            '}';
    }

    public PersonDTO getPerson() {
        return person;
    }

    public void setPerson(PersonDTO person) {
        this.person = person;
    }
}
