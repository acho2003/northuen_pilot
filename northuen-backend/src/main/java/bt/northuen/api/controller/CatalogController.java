package bt.northuen.api.controller;

import bt.northuen.api.dto.MarketplaceStatsResponse;
import bt.northuen.api.dto.ProductResponse;
import bt.northuen.api.dto.VendorResponse;
import bt.northuen.api.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogService catalogService;

    @GetMapping("/vendors")
    public List<VendorResponse> vendors(@RequestParam(required = false) String q, @RequestParam(required = false) String category) {
        return catalogService.vendors(q, category);
    }

    @GetMapping("/vendors/{id}")
    public VendorResponse vendor(@PathVariable UUID id) {
        return catalogService.vendor(id);
    }

    @GetMapping("/vendors/{id}/products")
    public List<ProductResponse> products(@PathVariable UUID id) {
        return catalogService.products(id);
    }

    @GetMapping("/products/search")
    public List<ProductResponse> searchProducts(@RequestParam(required = false) String q) {
        return catalogService.searchProducts(q);
    }

    @GetMapping("/marketplace/stats")
    public MarketplaceStatsResponse stats() {
        return catalogService.stats();
    }
}
