package com.pantheon.backend.repository;

import com.pantheon.backend.model.Platform;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformRepository extends JpaRepository<Platform, Integer> {

    Optional<Platform> findByName(String name);

}
