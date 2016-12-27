package com.accenture.videomanager.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

import com.accenture.videomanager.domain.enumeration.PictureType;

/**
 * A Picture.
 */
@Entity
@Table(name = "picture")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "picture")
public class Picture implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PictureType type;

    @NotNull
    @Size(max = 100)
    @Column(name = "tmdb_id", length = 100, nullable = false)
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

    public Picture type(PictureType type) {
        this.type = type;
        return this;
    }

    public void setType(PictureType type) {
        this.type = type;
    }

    public String getTmdbId() {
        return tmdbId;
    }

    public Picture tmdbId(String tmdbId) {
        this.tmdbId = tmdbId;
        return this;
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
        Picture picture = (Picture) o;
        if (picture.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, picture.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Picture{" +
            "id=" + id +
            ", type='" + type + "'" +
            ", tmdbId='" + tmdbId + "'" +
            '}';
    }
}
