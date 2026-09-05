package edu.meialua.kidsgrace.adapters.in.repositories;

import edu.meialua.kidsgrace.adapters.in.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndToyId(Long cartId, Long toyId);
}
