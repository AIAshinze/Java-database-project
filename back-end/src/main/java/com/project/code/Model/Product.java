package com.project.code.Model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product", uniqueConstraints = @UniqueConstraint(columnNames = "sku"))
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message = "Product name cannot be null")
    @NotBlank(message = "Product name cannot be blank")
    private String name;

    @NotNull(message = "Product category cannot be null")
    @NotBlank(message = "Product category cannot be blank")
    private String category;

    @NotNull(message = "Product price cannot be null")
    @PositiveOrZero(message = "Product price cannot be negative")
    private Double price;

    @NotNull(message = "Product SKU cannot be null")
    @NotBlank(message = "Product SKU cannot be blank")
    private String sku;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @JsonManagedReference("inventory-product")
    @JsonIgnore
    private List<Inventory> inventory = new ArrayList<>();

    protected Product() {
    }

    public Product(String name, String category, Double price, String sku) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.sku = sku;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public List<Inventory> getInventory() {
        return inventory;
    }

    public void setInventory(List<Inventory> inventory) {
        this.inventory = inventory;
    }
}
