package com.pantheon.backend.repository;

import com.pantheon.backend.model.Game;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Integer> {

    Optional<Game> findByIgdbId(String igdbId);

    Optional<Game> findByTitle(String title);

    List<Game> findByTitleContainingIgnoreCase(String title);

}
