package com.pantheon.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "library_entries", uniqueConstraints = {@UniqueConstraint(columnNames = {"game_id", "platform_id"})})
@Data
@NoArgsConstructor
public class LibraryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "platform_id")
    private Platform platform;

    @Column(name = "is_installed")
    private boolean isInstalled;

    private String installPath;

    private String platformGameId;

    private Integer playtimeMinutes = 0;

    private LocalDateTime lastPlayed;

}
