package bt.northuen.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(nullable = false)
    private boolean read = false;

    @Column(nullable = false, length = 40)
    private String type = "SYSTEM";

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role targetRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sent_by_admin_id")
    private User sentByAdmin;

    @Column(nullable = false)
    private int priority = 0;

    @Column
    private LocalDateTime readAt;
}
