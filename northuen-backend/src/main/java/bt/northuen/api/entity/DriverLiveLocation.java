package bt.northuen.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@IdClass(DriverLiveLocationId.class)
@Table(name = "driver_live_locations", indexes = {
        @Index(name = "idx_driver_live_order", columnList = "order_id"),
        @Index(name = "idx_driver_live_updated", columnList = "updated_at")
})
public class DriverLiveLocation {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PickDropOrder order;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(precision = 6, scale = 2)
    private BigDecimal heading;

    @Column(precision = 6, scale = 2)
    private BigDecimal speed;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = LocalDateTime.now();
    }
}
