package com.project.code.Controller;

import com.project.code.Model.CombinedRequest;
import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Model.Store;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Repo.StoreRepository;
import com.project.code.Service.ServiceClass;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;
    private final ServiceClass serviceClass;

    public InventoryController(
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            StoreRepository storeRepository,
            ServiceClass serviceClass) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.storeRepository = storeRepository;
        this.serviceClass = serviceClass;
    }

    @PutMapping
    public Map<String, String> updateInventory(@RequestBody CombinedRequest request) {
        if (request.getProduct() == null || request.getInventory() == null
                || request.getProduct().getId() <= 0
                || request.getInventory().getStore() == null
                || request.getInventory().getStore().getId() <= 0
                || request.getInventory().getStockLevel() == null
                || request.getInventory().getStockLevel() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Product, store, and a valid stock level are required.");
        }

        Product product = productRepository.findById(request.getProduct().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found."));
        Store store = storeRepository.findById(request.getInventory().getStore().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found."));
        Inventory existing = inventoryRepository.findByProductIdandStoreId(product.getId(), store.getId());
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No data available.");
        }

        Product changedProduct = request.getProduct();
        if (changedProduct.getName() != null) {
            product.setName(changedProduct.getName());
        }
        if (changedProduct.getCategory() != null) {
            product.setCategory(changedProduct.getCategory());
        }
        if (changedProduct.getPrice() != null) {
            product.setPrice(changedProduct.getPrice());
        }
        if (changedProduct.getSku() != null) {
            product.setSku(changedProduct.getSku());
        }
        productRepository.save(product);
        existing.setProduct(product);
        existing.setStore(store);
        existing.setStockLevel(request.getInventory().getStockLevel());
        inventoryRepository.save(existing);
        return Map.of("message", "Successfully updated product");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> saveInventory(@Valid @RequestBody Inventory request) {
        if (request.getProduct() == null || request.getStore() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product and store IDs are required.");
        }
        if (request.getProduct().getId() <= 0 || request.getStore().getId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product and store IDs must be positive.");
        }
        Product product = productRepository.findById(request.getProduct().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found."));
        Store store = storeRepository.findById(request.getStore().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found."));
        Inventory inventory = new Inventory(product, store, request.getStockLevel());

        if (!serviceClass.validateInventory(inventory)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inventory data is already present.");
        }
        inventoryRepository.save(inventory);
        return Map.of("message", "data saved successfully");
    }

    @GetMapping("/{storeId}")
    public Map<String, Object> getAllProducts(@PathVariable Long storeId) {
        requireStore(storeId);
        return Map.of("products", storeProducts(inventoryRepository.findByStore_Id(storeId)));
    }

    @GetMapping("/filter/{category}/{name}/{storeId}")
    public Map<String, Object> getProductName(
            @PathVariable String category,
            @PathVariable String name,
            @PathVariable Long storeId) {
        requireStore(storeId);
        List<Product> products;
        boolean noCategory = "null".equalsIgnoreCase(category);
        boolean noName = "null".equalsIgnoreCase(name);
        if (noCategory && noName) {
            products = productRepository.findProductsByStoreId(storeId);
        } else if (noCategory) {
            products = productRepository.findByNameLike(storeId, name);
        } else if (noName) {
            products = productRepository.findByCategoryAndStoreId(storeId, category);
        } else {
            products = productRepository.findByNameAndCategory(storeId, name, category);
        }
        return Map.of("product", storeProducts(products, storeId));
    }

    @GetMapping("/search/{name}/{storeId}")
    public Map<String, Object> searchProduct(@PathVariable String name, @PathVariable Long storeId) {
        requireStore(storeId);
        return Map.of("product", storeProducts(productRepository.findByNameLike(storeId, name), storeId));
    }

    @GetMapping("/search/{name}")
    public Map<String, Object> searchProductAcrossStores(@PathVariable String name) {
        return Map.of("product", productRepository.findProductBySubName(name));
    }

    @DeleteMapping("/{productId}")
    public Map<String, String> removeProduct(
            @PathVariable Long productId,
            @RequestParam(required = false) Long storeId) {
        if (!serviceClass.ValidateProductId(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not present in database.");
        }
        if (storeId == null) {
            inventoryRepository.deleteByProductId(productId);
        } else {
            inventoryRepository.deleteByProductIdAndStoreId(productId, storeId);
        }
        return Map.of("message", "Product removed from inventory successfully");
    }

    @GetMapping("/validate/{quantity}/{storeId}/{productId}")
    public boolean validateQuantity(
            @PathVariable Integer quantity,
            @PathVariable Long storeId,
            @PathVariable Long productId) {
        if (quantity == null || quantity <= 0) {
            return false;
        }
        Inventory inventory = inventoryRepository.findByProductIdandStoreId(productId, storeId);
        return inventory != null && inventory.getStockLevel() >= quantity;
    }

    private void requireStore(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found.");
        }
    }

    private List<Map<String, Object>> storeProducts(List<Inventory> inventory) {
        return inventory.stream()
                .map(item -> productResponse(item.getProduct(), item))
                .toList();
    }

    private List<Map<String, Object>> storeProducts(List<Product> products, Long storeId) {
        return products.stream()
                .map(product -> {
                    Inventory inventory = inventoryRepository.findByProductIdandStoreId(product.getId(), storeId);
                    return inventory == null ? null : productResponse(product, inventory);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private Map<String, Object> productResponse(Product product, Inventory inventory) {
        return Map.of(
                "id", product.getId(),
                "name", product.getName(),
                "category", product.getCategory(),
                "price", product.getPrice(),
                "sku", product.getSku(),
                "inventory", List.of(Map.of("id", inventory.getId(), "stockLevel", inventory.getStockLevel())));
    }
}
