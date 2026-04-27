package bt.northuen.api.service;

import bt.northuen.api.dto.*;
import bt.northuen.api.entity.OrderStatus;
import bt.northuen.api.entity.Product;
import bt.northuen.api.entity.User;
import bt.northuen.api.entity.Vendor;
import bt.northuen.api.exception.BusinessRuleException;
import bt.northuen.api.exception.ResourceNotFoundException;
import bt.northuen.api.repository.ProductRepository;
import bt.northuen.api.repository.OrderRepository;
import bt.northuen.api.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public List<VendorResponse> vendors(String query, String category) {
        var search = blankToNull(query);
        var selectedCategory = blankToNull(category);
        return vendorRepository.findAll().stream()
                .filter(vendor -> selectedCategory == null || vendor.getCategory().equalsIgnoreCase(selectedCategory))
                .filter(vendor -> search == null
                        || vendor.getName().toLowerCase().contains(search.toLowerCase())
                        || vendor.getDescription().toLowerCase().contains(search.toLowerCase())
                        || vendor.getAddress().toLowerCase().contains(search.toLowerCase()))
                .sorted((left, right) -> Boolean.compare(right.isOpen(), left.isOpen()))
                .map(mapper::vendor)
                .toList();
    }

    @Transactional(readOnly = true)
    public MarketplaceStatsResponse stats() {
        long openVendors = vendorRepository.findAll().stream().filter(Vendor::isOpen).count();
        long availableProducts = productRepository.findAll().stream().filter(Product::isAvailable).count();
        long activeOrders = orderRepository.findAll().stream()
                .filter(order -> !List.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED, OrderStatus.VENDOR_REJECTED).contains(order.getStatus()))
                .count();
        return new MarketplaceStatsResponse(openVendors, availableProducts, activeOrders);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String query) {
        var search = blankToNull(query);
        return productRepository.findAll().stream()
                .filter(Product::isAvailable)
                .filter(product -> search == null
                        || product.getName().toLowerCase().contains(search.toLowerCase())
                        || product.getDescription().toLowerCase().contains(search.toLowerCase()))
                .map(mapper::product)
                .toList();
    }

    @Transactional(readOnly = true)
    public VendorResponse vendor(UUID id) {
        return mapper.vendor(findVendor(id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> products(UUID vendorId) {
        return productRepository.findByVendorAndAvailableTrue(findVendor(vendorId)).stream().map(mapper::product).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> myProducts(User owner) {
        var vendor = vendorRepository.findByOwner(owner).orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found."));
        return productRepository.findByVendor(vendor).stream().map(mapper::product).toList();
    }

    @Transactional
    public VendorResponse upsertMyVendor(User owner, VendorRequest request) {
        var vendor = vendorRepository.findByOwner(owner).orElseGet(Vendor::new);
        vendor.setOwner(owner);
        vendor.setName(request.name());
        vendor.setCategory(request.category());
        vendor.setDescription(request.description());
        vendor.setAddress(request.address());
        vendor.setLatitude(request.latitude());
        vendor.setLongitude(request.longitude());
        vendor.setImageUrl(request.imageUrl());
        vendor.setOpen(request.open());
        return mapper.vendor(vendorRepository.save(vendor));
    }

    @Transactional
    public ProductResponse createProduct(User owner, ProductRequest request) {
        var vendor = vendorRepository.findByOwner(owner).orElseThrow(() -> new BusinessRuleException("Create a vendor profile first."));
        var product = new Product();
        applyProduct(product, vendor, request);
        return mapper.product(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(User owner, UUID productId, ProductRequest request) {
        var vendor = vendorRepository.findByOwner(owner).orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found."));
        var product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        if (!product.getVendor().getId().equals(vendor.getId())) {
            throw new BusinessRuleException("Product does not belong to your vendor profile.");
        }
        applyProduct(product, vendor, request);
        return mapper.product(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(User owner, UUID productId) {
        var vendor = vendorRepository.findByOwner(owner).orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found."));
        var product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        if (!product.getVendor().getId().equals(vendor.getId())) {
            throw new BusinessRuleException("Product does not belong to your vendor profile.");
        }
        productRepository.delete(product);
    }

    private void applyProduct(Product product, Vendor vendor, ProductRequest request) {
        product.setVendor(vendor);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(request.category());
        product.setImageUrl(request.imageUrl());
        product.setAvailable(request.available());
    }

    private Vendor findVendor(UUID id) {
        return vendorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vendor not found."));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
