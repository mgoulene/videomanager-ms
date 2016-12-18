package com.accenture.videomanager.service.impl;

import com.accenture.videomanager.domain.enumeration.PictureType;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeTypeUtils;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Created by vagrant on 12/14/16.
 */
@Service
@Transactional
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

    private DateTimeFormatter longDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private DateTimeFormatter shortDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy");

    @Override
    @Transactional(readOnly = true)
    public MovieDb findOne(int tmdbId) {
        log.debug("Request to get MovieDb : {}", tmdbId);

        return TmdbDataLoader.the().getMovie(tmdbId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieDb> searchTMDBMovies( String query) {
        log.debug("Request to search MovieDbs for query {}", query);

        return TmdbDataLoader.the().searchMovie(query);
    }

    @Override
    @Transactional
    public MovieDTO saveMovie(int tmbdId) {
        MovieDTO result = movieService.findOneByTmdbId(tmbdId);
        // If null, than we need to create one
        if (result == null) {
            MovieDb movieDb = TmdbDataLoader.the().getMovie(tmbdId);
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
            for (int i = 0; i < credits.getCast().size(); i++) {
                PersonCast personCast = credits.getCast().get(i);
                PersonDTO personDTO = savePersonFromTmdbId(personCast.getId());
                ActorDTO actorDTO = new ActorDTO();
                actorDTO.setActorCharacter(personCast.getCharacter());
                actorDTO.setActorOrder(personCast.getOrder());
                actorDTO.setPersonId(personDTO.getId());
                actorDTO.setMovieActorId(movieDTO.getId());
                actorDTO = actorService.save(actorDTO);
                movieDTO.getActors().add(actorDTO);

            }
            for (int i = 0; i < credits.getCrew().size(); i++) {
                PersonCrew personCrew = credits.getCrew().get(i);
                PersonDTO personDTO = savePersonFromTmdbId(personCrew.getId());
                CrewDTO crewDTO = new CrewDTO();
                crewDTO.setDepartment(personCrew.getDepartment());
                crewDTO.setJob(personCrew.getJob());
                crewDTO.setPersonId(personDTO.getId());
                crewDTO.setMovieCrewId(movieDTO.getId());
                crewDTO = crewService.save(crewDTO);
                movieDTO.getCrews().add(crewDTO);
            }
            if (movieDb.getPosterPath() != null && movieDb.getPosterPath() != "") {
                PictureDTO poster = savePictureFromTmdbPath(movieDb.getPosterPath(), PictureType.POSTER_MOVIE, null);
                movieDTO.setPosterId(poster.getId());
            }
            if (movieDb.getBackdropPath() != null && movieDb.getBackdropPath() != "") {
                PictureDTO backdrop = savePictureFromTmdbPath(movieDb.getBackdropPath(), PictureType.BACKDROP_MOVIE, null);
                movieDTO.setBackdropId(backdrop.getId());
            }
            MovieImages movieImages = TmdbDataLoader.the().getImages(movieDb.getId());
            for (int i = 0; i < movieImages.getPosters().size(); i++) {
                PictureDTO artworkPictureDTO = savePictureFromTmdbPath(movieImages.getPosters().get(i).getFilePath(), PictureType.ARTWORK, movieDTO.getId());
                movieDTO.getArtworks().add(artworkPictureDTO);
            }
            for (int i = 0; i < movieDb.getGenres().size();i++) {
                Genre genreDb = movieDb.getGenres().get(i);
                GenreDTO genreDTO = genreService.findOneByName(genreDb.getName());
                if (genreDTO == null) {
                    genreDTO =new GenreDTO();
                    genreDTO.setName(genreDb.getName());
                    genreDTO = genreService.save(genreDTO);

                }
                movieDTO.getGenres().add(genreDTO);
            }
            System.out.println("Creating Movie : " + movieDb.getTitle());
            result = movieService.save(movieDTO);
        }
        return result;

    }

    private PictureDTO savePictureFromTmdbPath(String tmdbPath, PictureType type, Long movieId) {
        byte[] bytes = TmdbDataLoader.the().getImageData(tmdbPath);
        PictureDTO pictureDTO = new PictureDTO();
        pictureDTO.setType(type);
        pictureDTO.setImageContentType(MimeTypeUtils.IMAGE_JPEG.getType());
        pictureDTO.setImage(bytes);
        pictureDTO.setMovieId(movieId);
        pictureDTO = pictureService.save(pictureDTO);
        return pictureDTO;

    }

    private LocalDate getLocalDate(String date) {
        LocalDate ld = null;
        date = date.length() == 4 ? date = date+"-01-01":date;
        if (date != null && date != "") {
            try {
                ld = LocalDate.parse(date, longDateTimeFormatter);
            } catch (DateTimeParseException e) {
                ld = null; //LocalDate.parse(date, shortDateTimeFormatter);
            }
        }
        return ld;
    }

    private PersonDTO savePersonFromTmdbId(int tmdbId) {
        PersonDTO personDTO = personService.findOneByTmdbId(tmdbId);
        if (personDTO == null) {
            PersonPeople personPeople = TmdbDataLoader.the().getPersonInfo(tmdbId);
            personDTO = new PersonDTO();
            personDTO.setHomepage(personPeople.getHomepage());
            personDTO.setBiography(personPeople.getBiography());
            personDTO.setBirthday(getLocalDate(personPeople.getBirthday()));
            personDTO.setDeathday(getLocalDate(personPeople.getDeathday()));
            personDTO.setName(personPeople.getName());

            personDTO.setTmdbId(personPeople.getId());
            if (personPeople.getProfilePath() != null && personPeople.getProfilePath() != "") {
                PictureDTO personProfilePictureDTO = savePictureFromTmdbPath(personPeople.getProfilePath(), PictureType.PEOPLE, null);
                personDTO.setProfilePictureId(personProfilePictureDTO.getId());
            }
            personDTO = personService.save(personDTO);
        }
        return personDTO;
    }
}
