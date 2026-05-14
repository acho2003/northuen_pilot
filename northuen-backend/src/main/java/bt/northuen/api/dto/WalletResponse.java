package bt.northuen.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        UUID userId,
        BigDecimal tokenBalance,
        String currency,
        String rechargeMode,
        List<WalletTransactionResponse> transactions
) {}
