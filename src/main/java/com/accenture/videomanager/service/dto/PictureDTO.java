package com.accenture.videomanager.service.dto;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

import com.accenture.videomanager.domain.enumeration.PictureType;

/**
 * A DTO for the Picture entity.
 */
public class PictureDTO implements Serializable {

    private Long id;

    @NotNull
    private PictureType type;

    @NotNull
    @Size(max = 100)
    private String tmdbId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public PictureType getType() {
        return type;
    }

    public void setType(PictureType type) {
        this.type = type;
    }
    public String getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(String tmdbId) {
        this.tmdbId = tmdbId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PictureDTO pictureDTO = (PictureDTO) o;

        if ( ! Objects.equals(id, pictureDTO.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "PictureDTO{" +
            "id=" + id +
            ", type='" + type + "'" +
            ", tmdbId='" + tmdbId + "'" +
            '}';
    }
}
