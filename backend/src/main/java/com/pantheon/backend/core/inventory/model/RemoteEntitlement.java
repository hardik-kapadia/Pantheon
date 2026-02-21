package com.pantheon.backend.core.inventory.model;

import com.pantheon.backend.core.platform.model.Platform;
import jakarta.persistence.CascadeType;
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

import java.util.Objects;

@Entity
@Table(name = "remote_entitlements", uniqueConstraints = {@UniqueConstraint(columnNames = {"platform_id", "platform_game_id"})})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RemoteEntitlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "platform_id")
    private Platform platform;

    @Column(name = "platform_game_id")
    private String platformGameId;

    @Builder.Default
    @Column(name = "playtime_minutes")
    private Integer playtimeMinutes = 0;

    @ToString.Exclude
    @OneToOne(mappedBy = "entitlement", cascade = CascadeType.ALL)
    private LocalInstallation localInstallation;

    @ToString.Include(name = "installation")
    private String getInstallationSummary() {
        if (localInstallation == null) return "None";
        return String.format("[%s] -> %s",
                localInstallation.getInstallFolder(),
                localInstallation.getExecutablePath());
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        RemoteEntitlement that = (RemoteEntitlement) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

}
