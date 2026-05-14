package bt.northuen.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "pickdrop_orders", indexes = {
        @Index(name = "idx_pickdrop_customer", columnList = "customer_id"),
        @Index(name = "idx_pickdrop_driver", columnList = "driver_id"),
        @Index(name = "idx_pickdrop_status", columnList = "status"),
        @Index(name = "idx_pickdrop_created", columnList = "created_at")
})
public class PickDropOrder extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(nullable = false, columnDefinition = "text")
    private String pickupAddress;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal pickupLat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal pickupLng;

    @Column(nullable = false, columnDefinition = "text")
    private String dropAddress;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal dropLat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal dropLng;

    @Column(nullable = false, length = 80)
    private String itemType;

    @Column(nullable = false, columnDefinition = "text")
    private String itemDescription;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal estimatedDistanceKm;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal estimatedPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PickDropStatus status = PickDropStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
}
