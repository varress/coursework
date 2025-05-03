package fi.secureprogramming.app.controller;

import fi.secureprogramming.app.service.ProductService;
import fi.secureprogramming.dto.ProductDTO;
import fi.secureprogramming.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
class ProductController {

    //TODO: Error handling

    @Autowired
    private ProductService service;

    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return service.getAllProducts().stream().map(ProductDTO::fromEntity).toList();
    }

    @PostMapping
    public ProductDTO addProduct(@RequestBody ProductDTO product) {
        Product newProduct = service.addProduct(product.toEntity());
        return ProductDTO.fromEntity(newProduct);
    }
}
