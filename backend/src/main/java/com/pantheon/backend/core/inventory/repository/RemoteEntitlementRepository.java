package com.pantheon.backend.core.inventory.repository;

import com.pantheon.backend.core.inventory.model.RemoteEntitlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RemoteEntitlementRepository extends JpaRepository<RemoteEntitlement, Long> {
}
