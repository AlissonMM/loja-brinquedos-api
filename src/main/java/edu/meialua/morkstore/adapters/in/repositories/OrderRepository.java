package edu.meialua.morkstore.adapters.in.repositories;

import edu.meialua.morkstore.adapters.in.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findAllByOrderByCreatedAtDesc();

    boolean existsByItems_Product_Id(Long productId);
}
