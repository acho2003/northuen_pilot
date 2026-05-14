package bt.northuen.api.repository;

import bt.northuen.api.entity.User;
import bt.northuen.api.entity.WalletAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletAccountRepository extends JpaRepository<WalletAccount, UUID> {
    Optional<WalletAccount> findByUser(User user);
}
