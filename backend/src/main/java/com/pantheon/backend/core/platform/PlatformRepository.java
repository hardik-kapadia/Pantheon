package com.pantheon.backend.core.platform;

import com.pantheon.backend.core.platform.model.Platform;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface PlatformService extends JpaRepository<Platform, Integer> {

    Optional<Platform> findByName(String name);

}
