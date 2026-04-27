package bt.northuen.api.repository;

import bt.northuen.api.entity.Cart;
import bt.northuen.api.entity.CartItem;
import bt.northuen.api.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
