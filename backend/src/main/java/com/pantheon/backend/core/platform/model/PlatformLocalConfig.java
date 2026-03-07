package com.pantheon.backend.core.platform.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "platform_local_configs")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PlatformLocalConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "executable_path")
    private String executablePath;

    @Column(name = "manifests_path")
    private String manifestsPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "local_scan_strategy", nullable = false)
    private ScanStrategy localScanStrategy;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "platform_id", referencedColumnName = "id")
    private Platform platform;

}
