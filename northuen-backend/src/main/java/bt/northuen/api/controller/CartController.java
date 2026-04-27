package bt.northuen.api.controller;

import bt.northuen.api.dto.AddCartItemRequest;
import bt.northuen.api.dto.CartResponse;
import bt.northuen.api.dto.UpdateCartItemRequest;
import bt.northuen.api.security.CurrentUser;
import bt.northuen.api.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public CartResponse get() {
        return cartService.get(CurrentUser.get());
    }

    @PostMapping("/items")
    public CartResponse addItem(@Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(CurrentUser.get(), request);
    }

    @PatchMapping("/items/{productId}")
    public CartResponse updateItem(@PathVariable UUID productId, @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(CurrentUser.get(), productId, request);
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(@PathVariable UUID productId) {
        return cartService.removeItem(CurrentUser.get(), productId);
    }

    @DeleteMapping
    public CartResponse clear() {
        return cartService.clear(CurrentUser.get());
    }
}
