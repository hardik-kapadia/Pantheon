package com.pantheon.backend.core.platform.repository;

import com.pantheon.backend.core.platform.model.Platform;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformRepository extends JpaRepository<Platform, Integer> {

    Optional<Platform> findByName(String name);

}
