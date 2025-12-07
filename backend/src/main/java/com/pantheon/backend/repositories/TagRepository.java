package com.pantheon.backend.repositories;

import com.pantheon.backend.model.Tag;

import java.util.Optional;

public interface TagRepository {

    Optional<Tag> findByName(String name);

}
