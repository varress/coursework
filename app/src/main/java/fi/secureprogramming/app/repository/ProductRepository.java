package fi.secureprogramming.app.repository;

import fi.secureprogramming.app.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {}
