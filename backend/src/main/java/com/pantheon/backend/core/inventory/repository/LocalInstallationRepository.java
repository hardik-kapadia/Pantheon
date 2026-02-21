package com.pantheon.backend.core.inventory.repository;

import com.pantheon.backend.core.inventory.model.LocalInstallation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalInstallationRepository extends JpaRepository<LocalInstallation, Long> {
}
