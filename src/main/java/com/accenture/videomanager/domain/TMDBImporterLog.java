package com.accenture.videomanager.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A TMDBImporterLog.
 */
@Entity
@Table(name = "tmdbimporter_log")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "tmdbimporterlog")
public class TMDBImporterLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "tmdb_id", nullable = false)
    private Integer tmdbId;

    @NotNull
    @Column(name = "number_of_people", nullable = false)
    private Integer numberOfPeople = 0;

    @NotNull
    @Column(name = "number_of_imported_people", nullable = false)
    private Integer numberOfImportedPeople = 0;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private ZonedDateTime startTime;

    @NotNull
    @Column(name = "import_duration", nullable = false)
    private Long importDuration;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTmdbId() {
        return tmdbId;
    }

    public TMDBImporterLog tmdbId(Integer tmdbId) {
        this.tmdbId = tmdbId;
        return this;
    }

    public void setTmdbId(Integer tmdbId) {
        this.tmdbId = tmdbId;
    }

    public Integer getNumberOfPeople() {
        return numberOfPeople;
    }

    public TMDBImporterLog numberOfPeople(Integer numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
        return this;
    }

    public void setNumberOfPeople(Integer numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public Integer getNumberOfImportedPeople() {
        return numberOfImportedPeople;
    }

    public TMDBImporterLog numberOfImportedPeople(Integer numberOfImportedPeople) {
        this.numberOfImportedPeople = numberOfImportedPeople;
        return this;
    }

    public void setNumberOfImportedPeople(Integer numberOfImportedPeople) {
        this.numberOfImportedPeople = numberOfImportedPeople;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public TMDBImporterLog startTime(ZonedDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public Long getImportDuration() {
        return importDuration;
    }

    public TMDBImporterLog importDuration(Long importDuration) {
        this.importDuration = importDuration;
        return this;
    }

    public void setImportDuration(Long importDuration) {
        this.importDuration = importDuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TMDBImporterLog tMDBImporterLog = (TMDBImporterLog) o;
        if (tMDBImporterLog.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, tMDBImporterLog.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "TMDBImporterLog{" +
            "id=" + id +
            ", tmdbId='" + tmdbId + "'" +
            ", numberOfPeople='" + numberOfPeople + "'" +
            ", numberOfImportedPeople='" + numberOfImportedPeople + "'" +
            ", startTime='" + startTime + "'" +
            ", importDuration='" + importDuration + "'" +
            '}';
    }


    public void start(int tmdbId) {
        startTime = ZonedDateTime.now();
        this.tmdbId = tmdbId;
    }

    public void addPerson() {
        numberOfPeople++;
    }

    public void addImportedPerson() {
        numberOfImportedPeople++;
    }

    public void stop() {
        importDuration = Duration.between(startTime, ZonedDateTime.now()).toMillis();
    }

}
