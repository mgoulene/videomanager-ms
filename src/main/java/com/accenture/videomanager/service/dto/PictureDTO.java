package com.accenture.videomanager.service.dto;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Lob;

import com.accenture.videomanager.domain.enumeration.PictureType;

/**
 * A DTO for the Picture entity.
 */
public class PictureDTO implements Serializable {

    private Long id;

    @NotNull
    private PictureType type;

    @Lob
    private byte[] image;

    private String imageContentType;
    @NotNull
    @Size(max = 100)
    private String tmdb_id;


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
    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getImageContentType() {
        return imageContentType;
    }

    public void setImageContentType(String imageContentType) {
        this.imageContentType = imageContentType;
    }
    public String getTmdb_id() {
        return tmdb_id;
    }

    public void setTmdb_id(String tmdb_id) {
        this.tmdb_id = tmdb_id;
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
            ", image='" + image + "'" +
            ", tmdb_id='" + tmdb_id + "'" +
            '}';
    }
}
