package com.pantheon.backend.core.library.repository;

import com.pantheon.backend.core.library.model.Library;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryRepository extends JpaRepository<Library, Long> {
}
