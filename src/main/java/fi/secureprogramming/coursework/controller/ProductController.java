package fi.secureprogramming.coursework.controller;

import fi.secureprogramming.coursework.dto.ProductDTO;
import fi.secureprogramming.coursework.model.Product;
import fi.secureprogramming.coursework.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
class ProductController {

    @Autowired
    private ProductService service;

    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return service.getAllProducts().stream().map(ProductDTO::fromEntity).toList();
    }

    @PostMapping
    public Product addProduct(@RequestBody ProductDTO product) {
        return service.addProduct(product.toEntity());
    }
}
