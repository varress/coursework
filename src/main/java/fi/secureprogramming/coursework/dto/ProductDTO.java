package fi.secureprogramming.coursework.dto;

import fi.secureprogramming.coursework.model.Product;

public class ProductDTO {

    private Long id;
    private String name;
    private double price;
    private String description;

    public ProductDTO() {
    }

    public ProductDTO(String name, double price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public ProductDTO(Long id, String name, double price, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public static ProductDTO fromEntity(Product product) {
        if (product == null) {
            return null;
        }

        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription()
        );
    }

    public Product toEntity() {
        Product product = new Product();
        product.setName(this.name);
        product.setPrice(this.price);
        product.setDescription(this.description);
        return product;
    }
}