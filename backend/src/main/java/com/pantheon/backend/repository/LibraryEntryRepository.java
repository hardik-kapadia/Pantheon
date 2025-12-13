package com.pantheon.backend.repository;

import com.pantheon.backend.model.LibraryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LibraryEntryRepository extends JpaRepository<LibraryEntry, Long> {

    Optional<LibraryEntry> findByGameIdAndPlatformId(Integer gameId, Integer platformId);

    List<LibraryEntry> findByPlatformId(Integer platformId);

    List<LibraryEntry> findByIsInstalledTrue();

}
