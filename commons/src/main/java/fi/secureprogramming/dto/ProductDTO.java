package fi.secureprogramming.dto;

import lombok.Data;

@Data
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
}