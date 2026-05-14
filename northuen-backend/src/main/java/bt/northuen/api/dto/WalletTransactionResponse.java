package bt.northuen.api.dto;

import bt.northuen.api.entity.WalletTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WalletTransactionResponse(
        UUID id,
        WalletTransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String reference,
        String note,
        LocalDateTime createdAt
) {}
