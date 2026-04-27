package bt.northuen.api.service;

import bt.northuen.api.dto.AddCartItemRequest;
import bt.northuen.api.dto.CartResponse;
import bt.northuen.api.dto.UpdateCartItemRequest;
import bt.northuen.api.entity.Cart;
import bt.northuen.api.entity.CartItem;
import bt.northuen.api.entity.User;
import bt.northuen.api.exception.BusinessRuleException;
import bt.northuen.api.exception.ResourceNotFoundException;
import bt.northuen.api.repository.CartItemRepository;
import bt.northuen.api.repository.CartRepository;
import bt.northuen.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public CartResponse get(User customer) {
        return mapper.cart(cartRepository.findByCustomer(customer).orElseGet(() -> emptyCart(customer)));
    }

    @Transactional
    public CartResponse addItem(User customer, AddCartItemRequest request) {
        var product = productRepository.findById(request.productId()).orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        if (!product.isAvailable() || !product.getVendor().isOpen()) {
            throw new BusinessRuleException("Product is not currently available.");
        }
        var cart = cartRepository.findByCustomer(customer).orElseGet(() -> cartRepository.save(emptyCart(customer)));
        if (cart.getVendor() != null && !cart.getVendor().getId().equals(product.getVendor().getId())) {
            cart.getItems().clear();
        }
        cart.setVendor(product.getVendor());
        var item = cartItemRepository.findByCartAndProduct(cart, product).orElseGet(CartItem::new);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(item.getQuantity() + request.quantity());
        if (!cart.getItems().contains(item)) {
            cart.getItems().add(item);
        }
        cartRepository.save(cart);
        return mapper.cart(cart);
    }

    @Transactional
    public CartResponse updateItem(User customer, UUID productId, UpdateCartItemRequest request) {
        var cart = cartRepository.findByCustomer(customer).orElseThrow(() -> new ResourceNotFoundException("Cart not found."));
        var product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        var item = cartItemRepository.findByCartAndProduct(cart, product).orElseThrow(() -> new ResourceNotFoundException("Cart item not found."));
        if (request.quantity() == 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(request.quantity());
            cartItemRepository.save(item);
        }
        if (cart.getItems().isEmpty()) {
            cart.setVendor(null);
        }
        return mapper.cart(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(User customer, UUID productId) {
        return updateItem(customer, productId, new UpdateCartItemRequest(0));
    }

    @Transactional
    public CartResponse clear(User customer) {
        var cart = cartRepository.findByCustomer(customer).orElseGet(() -> cartRepository.save(emptyCart(customer)));
        cart.getItems().clear();
        cart.setVendor(null);
        return mapper.cart(cartRepository.save(cart));
    }

    private Cart emptyCart(User customer) {
        var cart = new Cart();
        cart.setCustomer(customer);
        return cart;
    }
}
