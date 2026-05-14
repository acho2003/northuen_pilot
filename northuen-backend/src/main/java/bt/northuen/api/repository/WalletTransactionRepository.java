package bt.northuen.api.repository;

import bt.northuen.api.entity.WalletAccount;
import bt.northuen.api.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
    List<WalletTransaction> findTop20ByWalletOrderByCreatedAtDesc(WalletAccount wallet);
    boolean existsByReference(String reference);
}
