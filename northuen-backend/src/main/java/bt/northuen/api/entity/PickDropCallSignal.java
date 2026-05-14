package bt.northuen.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "pickdrop_call_signals", indexes = {
        @Index(name = "idx_pickdrop_call_signals_call_time", columnList = "call_id,created_at")
})
public class PickDropCallSignal extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_id", nullable = false)
    private PickDropCallSession call;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;
}
