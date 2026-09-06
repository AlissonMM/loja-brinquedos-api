package edu.meialua.morkstore.adapters.in.repositories;

import edu.meialua.morkstore.adapters.in.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAll();

    Optional<Product> findById(Long id);

    Optional<Product> findByName(String names);
    List<Product> findAllByBrand(String brand);
    List<Product> findAllByCategory(String category);

    @Override
    void deleteById(Long id);


}
