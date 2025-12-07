package com.pantheon.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "platforms")
@Data
@NoArgsConstructor
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String name;

    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlatformType type;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "platform_paths",
            joinColumns = @JoinColumn(name = "platform_id")
    )
    @Column(name = "path") // The column name in the child table
    private List<String> libraryPaths = new ArrayList<>();

}