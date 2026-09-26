package edu.meialua.morkstore.adapters.in.repositories;

import edu.meialua.morkstore.adapters.in.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);
}
