package fi.secureprogramming.coursework.repository;

import fi.secureprogramming.coursework.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {}
