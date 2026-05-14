package bt.northuen.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "wallet_accounts", indexes = {
        @Index(name = "idx_wallet_accounts_user", columnList = "user_id", unique = true)
})
public class WalletAccount extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tokenBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;
}
