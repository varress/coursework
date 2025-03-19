package fi.secureprogramming.app.controller;

import fi.secureprogramming.app.dto.ProductDTO;
import fi.secureprogramming.app.model.Product;
import fi.secureprogramming.app.service.ProductService;
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
