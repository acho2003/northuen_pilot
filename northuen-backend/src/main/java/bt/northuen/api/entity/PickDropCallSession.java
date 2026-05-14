package bt.northuen.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "pickdrop_call_sessions", indexes = {
        @Index(name = "idx_pickdrop_calls_order_status", columnList = "order_id,status"),
        @Index(name = "idx_pickdrop_calls_caller", columnList = "caller_id"),
        @Index(name = "idx_pickdrop_calls_receiver", columnList = "receiver_id")
})
public class PickDropCallSession extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PickDropOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_id", nullable = false)
    private User caller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PickDropCallStatus status = PickDropCallStatus.RINGING;

    private LocalDateTime endedAt;
}
