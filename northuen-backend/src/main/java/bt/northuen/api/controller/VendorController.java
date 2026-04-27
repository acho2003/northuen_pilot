package bt.northuen.api.controller;

import bt.northuen.api.dto.*;
import bt.northuen.api.security.CurrentUser;
import bt.northuen.api.service.CatalogService;
import bt.northuen.api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vendor")
@RequiredArgsConstructor
public class VendorController {
    private final CatalogService catalogService;
    private final OrderService orderService;

    @PutMapping("/profile")
    public VendorResponse upsertProfile(@Valid @RequestBody VendorRequest request) {
        return catalogService.upsertMyVendor(CurrentUser.get(), request);
    }

    @GetMapping("/products")
    public List<ProductResponse> products() {
        return catalogService.myProducts(CurrentUser.get());
    }

    @PostMapping("/products")
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return catalogService.createProduct(CurrentUser.get(), request);
    }

    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return catalogService.updateProduct(CurrentUser.get(), id, request);
    }

    @DeleteMapping("/products/{id}")
    public void deleteProduct(@PathVariable UUID id) {
        catalogService.deleteProduct(CurrentUser.get(), id);
    }

    @GetMapping("/orders")
    public List<OrderResponse> orders() {
        return orderService.vendorOrders(CurrentUser.get());
    }
}
