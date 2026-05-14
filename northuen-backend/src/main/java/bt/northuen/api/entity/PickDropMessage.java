package bt.northuen.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "pickdrop_messages", indexes = {
        @Index(name = "idx_pickdrop_messages_order_time", columnList = "order_id,created_at"),
        @Index(name = "idx_pickdrop_messages_sender", columnList = "sender_id")
})
public class PickDropMessage extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PickDropOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role senderRole;

    @Column(nullable = false, columnDefinition = "text")
    private String body;
}
