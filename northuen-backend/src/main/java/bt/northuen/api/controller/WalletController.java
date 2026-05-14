package bt.northuen.api.controller;

import bt.northuen.api.dto.WalletResponse;
import bt.northuen.api.security.CurrentUser;
import bt.northuen.api.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @GetMapping("/me")
    public WalletResponse mine() {
        return walletService.mine(CurrentUser.get());
    }
}
