package com.project.code.Service;

import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ServiceClass {
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public ServiceClass(ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public boolean validateInventory(Inventory inventory) {
        if (inventory == null || inventory.getProduct() == null || inventory.getStore() == null) {
            return false;
        }
        return inventoryRepository.findByProductIdandStoreId(
                inventory.getProduct().getId(), inventory.getStore().getId()) == null;
    }

    public boolean validateProduct(Product product) {
        return product != null
                && product.getName() != null
                && !product.getName().isBlank()
                && product.getSku() != null
                && !product.getSku().isBlank()
                && productRepository.findByName(product.getName()) == null
                && productRepository.findBySku(product.getSku()).isEmpty();
    }

    public boolean ValidateProductId(long id) {
        return productRepository.existsById(id);
    }

    public Inventory getInventoryId(Inventory inventory) {
        if (inventory == null || inventory.getProduct() == null || inventory.getStore() == null) {
            return null;
        }
        return inventoryRepository.findByProductIdandStoreId(
                inventory.getProduct().getId(), inventory.getStore().getId());
    }
}
