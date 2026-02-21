package com.pantheon.backend.core.library.model;

import com.pantheon.backend.core.inventory.model.LocalInstallation;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "libraries")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Library {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "platform_id")
    @ToString.Exclude
    private Platform platform;

    private String path;

    @Builder.Default
    @Column(name = "is_global")
    private boolean isGlobal = false;

    @Builder.Default
    @Column(name = "is_accessible")
    private boolean isAccessible = true;

    @Column(name = "last_scanned")
    private LocalDateTime lastScanned;

    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<LocalInstallation> installations = new ArrayList<>();

    @AssertTrue(message = "Path must be provided for non-global libraries")
    public boolean isPathValid() {
        return isGlobal || (path != null && !path.isBlank());
    }

    @ToString.Include(name = "installation")
    private String getInstallationSummary() {
        return platform.getName();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Library that = (Library) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
