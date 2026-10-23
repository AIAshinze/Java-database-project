package com.project.code.Controller;

import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Service.ServiceClass;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductRepository productRepository;
    private final ServiceClass serviceClass;
    private final InventoryRepository inventoryRepository;

    public ProductController(
            ProductRepository productRepository,
            ServiceClass serviceClass,
            InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.serviceClass = serviceClass;
        this.inventoryRepository = inventoryRepository;
    }

    @PostMapping
    public Map<String, String> addProduct(@Valid @RequestBody Product product) {
        if (!serviceClass.validateProduct(product)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A product with this name or SKU already exists.");
        }
        Product saved = productRepository.save(product);
        return Map.of("message", "Product added successfully with ID " + saved.getId());
    }

    @GetMapping({"/product/{id}", "/{id}"})
    public Map<String, Object> getProductbyId(@PathVariable("id") Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found."));
        return Map.of("products", productResponse(product));
    }

    @PutMapping({"", "/{id}"})
    public Map<String, String> updateProduct(
            @PathVariable(value = "id", required = false) Long id,
            @Valid @RequestBody Product body) {
        Long productId = id != null ? id : body.getId();
        if (productId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID is required.");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found."));
        Product existingName = productRepository.findByName(body.getName());
        if (existingName != null && existingName.getId() != product.getId()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A product with this name already exists.");
        }
        boolean duplicateSku = productRepository.findBySku(body.getSku()).stream()
                .anyMatch(existing -> existing.getId() != product.getId());
        if (duplicateSku) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A product with this SKU already exists.");
        }
        product.setName(body.getName());
        product.setCategory(body.getCategory());
        product.setPrice(body.getPrice());
        product.setSku(body.getSku());
        productRepository.save(product);
        return Map.of("message", "Product updated successfully");
    }

    @GetMapping("/category/{name}/{category}")
    public Map<String, Object> filterbyCategoryProduct(
            @PathVariable String name,
            @PathVariable String category) {
        boolean noName = "null".equalsIgnoreCase(name);
        boolean noCategory = "null".equalsIgnoreCase(category);
        List<Product> products;
        if (noName && noCategory) {
            products = productRepository.findAll();
        } else if (noName) {
            products = productRepository.findByCategory(category);
        } else if (noCategory) {
            products = productRepository.findProductBySubName(name);
        } else {
            products = productRepository.findProductBySubNameAndCategory(name, category);
        }
        return Map.of("products", products.stream().map(this::productResponse).toList());
    }

    @GetMapping
    public Map<String, Object> listProduct() {
        return Map.of("products", productRepository.findAll().stream().map(this::productResponse).toList());
    }

    @GetMapping("/filter/{category}/{storeId}")
    public Map<String, Object> getProductbyCategoryAndStoreId(
            @PathVariable String category,
            @PathVariable Long storeId) {
        return Map.of("product", productRepository.findProductByCategory(category, storeId)
                .stream().map(this::productResponse).toList());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, String> deleteProduct(@PathVariable("id") Long id) {
        if (!serviceClass.ValidateProductId(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not present in database.");
        }
        inventoryRepository.deleteByProductId(id);
        productRepository.deleteById(id);
        return Map.of("message", "Product deleted successfully");
    }

    @GetMapping("/searchProduct/{name}")
    public Map<String, Object> searchProduct(@PathVariable String name) {
        return Map.of("products", productRepository.findProductBySubName(name)
                .stream().map(this::productResponse).toList());
    }

    private Map<String, Object> productResponse(Product product) {
        return Map.of(
                "id", product.getId(),
                "name", product.getName(),
                "category", product.getCategory(),
                "price", product.getPrice(),
                "sku", product.getSku());
    }
}
