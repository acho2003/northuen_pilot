package bt.northuen.api.service;

import bt.northuen.api.dto.WalletResponse;
import bt.northuen.api.dto.WalletTransactionResponse;
import bt.northuen.api.entity.User;
import bt.northuen.api.entity.WalletAccount;
import bt.northuen.api.repository.WalletAccountRepository;
import bt.northuen.api.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletAccountRepository walletAccountRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Transactional
    public WalletResponse mine(User user) {
        var wallet = walletAccountRepository.findByUser(user).orElseGet(() -> {
            var account = new WalletAccount();
            account.setUser(user);
            account.setTokenBalance(BigDecimal.ZERO);
            return walletAccountRepository.save(account);
        });
        return toResponse(wallet);
    }

    private WalletResponse toResponse(WalletAccount wallet) {
        var transactions = walletTransactionRepository.findTop20ByWalletOrderByCreatedAtDesc(wallet).stream()
                .map(txn -> new WalletTransactionResponse(
                        txn.getId(),
                        txn.getType(),
                        txn.getAmount(),
                        txn.getBalanceAfter(),
                        txn.getReference(),
                        txn.getNote(),
                        txn.getCreatedAt()
                ))
                .toList();
        return new WalletResponse(
                wallet.getId(),
                wallet.getUser().getId(),
                wallet.getTokenBalance(),
                "NT",
                "MANUAL_SEED_RECHARGE",
                transactions
        );
    }
}
