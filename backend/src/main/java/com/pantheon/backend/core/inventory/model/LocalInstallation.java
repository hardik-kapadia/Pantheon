package com.pantheon.backend.core.inventory.model;

import com.pantheon.backend.core.library.model.Library;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "local_installations", uniqueConstraints = {@UniqueConstraint(columnNames = {"entitlement_id"})})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LocalInstallation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entitlement_id", nullable = false)
    private RemoteEntitlement entitlement;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    @Column(name = "install_folder", nullable = false)
    private String installFolder;

    @Column(name = "executable_path", nullable = false)
    private String executablePath;

    @Builder.Default
    @Column(name = "game_size_bytes")
    private Long gameSizeInBytes = 0L;

    @Builder.Default
    @Column(name = "is_valid")
    private boolean isValid = true;

    @Builder.Default
    @Column(name = "last_played")
    private LocalDateTime lastPlayed = null;

    @ToString.Include(name = "entitlementInfo")
    private String getEntitlementSummary() {
        if (entitlement == null) return "N/A";

        String title = (entitlement.getGame() != null) ? entitlement.getGame().getTitle() : "Unknown Game";

        return String.format("Title: %s, Playtime: %dm, PlatformID: %s",
                title,
                entitlement.getPlaytimeMinutes(),
                entitlement.getPlatformGameId());
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        LocalInstallation that = (LocalInstallation) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
