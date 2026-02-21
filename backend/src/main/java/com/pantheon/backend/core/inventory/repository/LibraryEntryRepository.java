package com.pantheon.backend.core.inventory.repository;

import com.pantheon.backend.core.inventory.model.LocalInstallation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LibraryEntryRepository extends JpaRepository<LocalInstallation, Long> {

    Optional<LocalInstallation> findByGameIdAndPlatformId(Integer gameId, Integer platformId);

    List<LocalInstallation> findByPlatformId(Integer platformId);

    List<LocalInstallation> findByIsInstalledTrue();

}
