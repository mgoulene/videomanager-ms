package com.accenture.videomanager.service.impl;

import com.accenture.videomanager.domain.TMDBImporterLog;
import com.accenture.videomanager.domain.enumeration.PictureType;
import com.accenture.videomanager.repository.TMDBImporterLogRepository;
import com.accenture.videomanager.service.*;
import com.accenture.videomanager.service.dto.*;
import com.accenture.videomanager.service.tmdb.TmdbDataLoader;
import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.MovieImages;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import info.movito.themoviedbapi.model.people.PersonPeople;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.MimeTypeUtils;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Implementation of the Interface TMDBMovieService
 */
@Service
public class TMDBMovieServiceImpl implements TMDBMovieService {
    private final Logger log = LoggerFactory.getLogger(TMDBMovieServiceImpl.class);
    @Inject
    private MovieService movieService;
    @Inject
    private PictureService pictureService;
    @Inject
    private PersonService personService;
    @Inject
    private ActorService actorService;
    @Inject
    private CrewService crewService;
    @Inject
    private GenreService genreService;
    @Inject
    private TMDBImporterLogRepository tmdbImporterLogRepository;

    private DateTimeFormatter longDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Find Movie data from TMDB
     *
     * @param tmdbId the id of the movie
     * @return the movie data
     */
    @Override
    @Transactional(readOnly = true)
    public MovieDb findOne(int tmdbId) {
        log.debug("Request to get MovieDb : {}", tmdbId);

        return TmdbDataLoader.the().getMovie(tmdbId);
    }

    /**
     * Find movies from a quesry
     *
     * @param query the movie query
     * @return list of movie data
     */
    @Override
    @Transactional(readOnly = true)
    public List<MovieDb> searchTMDBMovies( String query) {
        log.debug("Request to search MovieDbs for query {}", query);
        return TmdbDataLoader.the().searchMovie(query);
    }


    /**
     * Save movie data from a TMDB movie id
     *
     * @param tmbdId the id of the movie
     * @return the saved DTO of the movie
     */
    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public MovieDTO saveMovie(int tmbdId) {
        log.debug("-> Import TMDBMovie : "+tmbdId+"-start");
        MovieDTO result = movieService.findOneByTmdbId(tmbdId);
        // If null, than we need to create one
        if (result == null) {
            // retrieve the movie from TMDB
            MovieDb movieDb = TmdbDataLoader.the().getMovie(tmbdId);
            // if it exists, then create and save the DTO
            if (movieDb != null) {
                TMDBImporterLog importLog = new TMDBImporterLog();
                importLog.start(movieDb.getId());
                MovieDTO movieDTO = new MovieDTO();
                movieDTO.setTitle(movieDb.getTitle());
                movieDTO = movieService.save(movieDTO);
                movieDTO.setOriginalTitle(movieDb.getOriginalTitle());
                movieDTO.setOverview(movieDb.getOverview());
                movieDTO.setTmdbId(movieDb.getId());
                movieDTO.setBudget(movieDb.getBudget());
                movieDTO.setHomepage(movieDb.getHomepage());
                movieDTO.setReleaseDate(getLocalDate(movieDb.getReleaseDate()));
                movieDTO.setRevenue(movieDb.getRevenue());
                movieDTO.setRuntime(movieDb.getRuntime());
                movieDTO.setVoteCount(movieDb.getVoteCount());
                movieDTO.setVoteRating(movieDb.getVoteAverage());
                Credits credits = TmdbDataLoader.the().getCredits(tmbdId);
                // Creates the Actors
                for (int i = 0; i < credits.getCast().size(); i++) {
                    PersonCast personCast = credits.getCast().get(i);
                    PersonDTO personDTO = savePersonFromTmdbId(personCast.getId(), importLog);
                    ActorDTO actorDTO = new ActorDTO();
                    actorDTO.setActorCharacter(personCast.getCharacter());
                    actorDTO.setActorOrder(personCast.getOrder());
                    actorDTO.setPersonId(personDTO.getId());
                    actorDTO.setMovieActorId(movieDTO.getId());
                    actorDTO = actorService.save(actorDTO);
                    movieDTO.getActors().add(actorDTO);

                }
                // Creates the Crew
                for (int i = 0; i < credits.getCrew().size(); i++) {
                    PersonCrew personCrew = credits.getCrew().get(i);
                    PersonDTO personDTO = savePersonFromTmdbId(personCrew.getId(), importLog);
                    CrewDTO crewDTO = new CrewDTO();
                    crewDTO.setDepartment(personCrew.getDepartment());
                    crewDTO.setJob(personCrew.getJob());
                    crewDTO.setPersonId(personDTO.getId());
                    crewDTO.setMovieCrewId(movieDTO.getId());
                    crewDTO = crewService.save(crewDTO);
                    movieDTO.getCrews().add(crewDTO);
                }
                // Creates the Poster
                if (movieDb.getPosterPath() != null && movieDb.getPosterPath() != "") {
                    PictureDTO poster = savePictureFromTmdbPath(movieDb.getPosterPath(), PictureType.POSTER_MOVIE);
                    movieDTO.setPosterId(poster.getId());
                }
                // Creates the Backdrop
                if (movieDb.getBackdropPath() != null && movieDb.getBackdropPath() != "") {
                    PictureDTO backdrop = savePictureFromTmdbPath(movieDb.getBackdropPath(), PictureType.BACKDROP_MOVIE);
                    movieDTO.setBackdropId(backdrop.getId());
                }
                // Creates the Artworks
                MovieImages movieImages = TmdbDataLoader.the().getImages(movieDb.getId());
                for (int i = 0; i < movieImages.getPosters().size(); i++) {
                    PictureDTO artworkPictureDTO = savePictureFromTmdbPath(movieImages.getPosters().get(i).getFilePath(), PictureType.ARTWORK);
                    movieDTO.getArtworks().add(artworkPictureDTO);
                }
                // Creates the Genre
                for (int i = 0; i < movieDb.getGenres().size(); i++) {
                    Genre genreDb = movieDb.getGenres().get(i);
                    GenreDTO genreDTO = genreService.findOneByName(genreDb.getName());
                    if (genreDTO == null) {
                        genreDTO = new GenreDTO();
                        genreDTO.setName(genreDb.getName());
                        genreDTO = genreService.save(genreDTO);

                    }
                    movieDTO.getGenres().add(genreDTO);
                }
                //Save the Movie
                result = movieService.save(movieDTO);
                importLog.stop();
                tmdbImporterLogRepository.save(importLog);
                log.debug("-> Import TMDBMovie "+tmbdId+" - end");
            } else {
                log.debug("-> Import TMDBMovie "+tmbdId+" - does not exist");
            }

        } else {
            log.debug("-> Import TMDBMovie "+tmbdId+" - already importer");
        }

        return result;

    }

    /**
     * Saves the Picture Data from its tmdbPath
     * @param tmdbPath the path
     * @param type the type
     * @return the saved Picture
     */
    private PictureDTO savePictureFromTmdbPath(String tmdbPath, PictureType type) {
        log.debug("--> Import Picture "+tmdbPath+" - start");
        PictureDTO pictureDTO = new PictureDTO();
        pictureDTO.setType(type);
        // Truncate the start "/" and end ".jpg"
        pictureDTO.setTmdbId(tmdbPath.substring(1,tmdbPath.length()-4));
        pictureDTO = pictureService.save(pictureDTO);
        log.debug("--> Import Picture "+tmdbPath+" - end");
        return pictureDTO;

    }

    /**
     * Convert string date to LocalDate
     * @param date The string date. Format is "yyyy-MM-dd" or "yyyy"
     * @return The corresponding LocalDate
     */
    private LocalDate getLocalDate(String date) {
        LocalDate ld = null;
        // if size is 4, the date is "yyyy". Appends "-01-01"
        date = date.length() == 4 ? date = date+"-01-01":date;
        if (date != null && date != "") {
            try {
                ld = LocalDate.parse(date, longDateTimeFormatter);
            } catch (DateTimeParseException e) {
                ld = null;
            }
        }
        return ld;
    }

    /**
     * Save Person from tmdb Id
     * @param tmdbId the profile TMDB id
     * @return the saved Person
     */
    private PersonDTO savePersonFromTmdbId(int tmdbId, TMDBImporterLog importLog) {
        log.debug("--> Import Person "+tmdbId+" - start");
        importLog.addPerson();
        // Find if exists
        PersonDTO personDTO = personService.findOneByTmdbId(tmdbId);
        // If it does not exist, then create it
        if (personDTO == null) {
            // Load from TMDB
            PersonPeople personPeople = TmdbDataLoader.the().getPersonInfo(tmdbId);
            if (personPeople != null) {
                importLog.addImportedPerson();
                personDTO = new PersonDTO();
                personDTO.setHomepage(personPeople.getHomepage());
                personDTO.setBiography(personPeople.getBiography());
                personDTO.setBirthday(getLocalDate(personPeople.getBirthday()));
                personDTO.setDeathday(getLocalDate(personPeople.getDeathday()));
                personDTO.setName(personPeople.getName());
                personDTO.setTmdbId(personPeople.getId());
                // Get the Profile Picture
                if (personPeople.getProfilePath() != null && personPeople.getProfilePath() != "") {
                    PictureDTO personProfilePictureDTO = savePictureFromTmdbPath(personPeople.getProfilePath(), PictureType.PEOPLE);
                    personDTO.setProfilePictureId(personProfilePictureDTO.getId());
                }
                // Save it
                personDTO = personService.save(personDTO);
                log.debug("--> Import Person "+tmdbId+" - end");
            } else {
                log.debug("--> Import Person "+tmdbId+" - does not exist");
            }
        } else {
            log.debug("--> Import Person "+tmdbId+" - already exist");
        }
        return personDTO;
    }
}
