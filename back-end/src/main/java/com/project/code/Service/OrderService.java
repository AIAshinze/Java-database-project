package com.project.code.Service;

import com.project.code.Model.Customer;
import com.project.code.Model.Inventory;
import com.project.code.Model.OrderDetails;
import com.project.code.Model.OrderItem;
import com.project.code.Model.PlaceOrderRequestDTO;
import com.project.code.Model.Product;
import com.project.code.Model.PurchaseProductDTO;
import com.project.code.Model.Store;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.OrderDetailsRepository;
import com.project.code.Repo.OrderItemRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Repo.StoreRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            CustomerRepository customerRepository,
            StoreRepository storeRepository,
            OrderDetailsRepository orderDetailsRepository,
            OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.customerRepository = customerRepository;
        this.storeRepository = storeRepository;
        this.orderDetailsRepository = orderDetailsRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public void saveOrder(PlaceOrderRequestDTO placeOrderRequest) {
        if (placeOrderRequest == null
                || placeOrderRequest.getStoreId() == null
                || placeOrderRequest.getPurchaseProduct() == null
                || placeOrderRequest.getPurchaseProduct().isEmpty()) {
            throw new IllegalArgumentException("An order must include a store and at least one product.");
        }
        if (isBlank(placeOrderRequest.getCustomerName())
                || isBlank(placeOrderRequest.getCustomerEmail())
                || isBlank(placeOrderRequest.getCustomerPhone())) {
            throw new IllegalArgumentException("Customer name, email, and phone are required.");
        }

        Store store = storeRepository.findById(placeOrderRequest.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("Store not found."));
        String email = placeOrderRequest.getCustomerEmail().trim().toLowerCase(Locale.ROOT);
        Customer customer = customerRepository.findByEmail(email);
        if (customer == null) {
            customer = customerRepository.save(new Customer(
                        placeOrderRequest.getCustomerName().trim(),
                        email,
                        placeOrderRequest.getCustomerPhone().trim()));
        }

        List<Inventory> inventoryEntries = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        Map<Long, Long> requestedQuantities = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseProductDTO purchase : placeOrderRequest.getPurchaseProduct()) {
            if (purchase == null || purchase.getId() == null || purchase.getQuantity() == null
                    || purchase.getQuantity() <= 0) {
                throw new IllegalArgumentException("Each order item must have a product ID and a positive quantity.");
            }

            Product product = productRepository.findById(purchase.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product not found: " + purchase.getId()));
            Inventory inventory = inventoryRepository.findForUpdateByProductIdAndStoreId(
                            product.getId(), store.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product " + product.getId() + " is not stocked at this store."));
            long requestedQuantity = requestedQuantities.getOrDefault(product.getId(), 0L)
                    + purchase.getQuantity();
            if (inventory.getStockLevel() < requestedQuantity) {
                throw new IllegalArgumentException(
                        "Insufficient stock for product " + product.getId() + ".");
            }
            requestedQuantities.put(product.getId(), requestedQuantity);
            inventoryEntries.add(inventory);
            products.add(product);
            total = total.add(BigDecimal.valueOf(product.getPrice())
                    .multiply(BigDecimal.valueOf(purchase.getQuantity())));
        }

        OrderDetails order = orderDetailsRepository.save(new OrderDetails(
                customer, store, total.doubleValue(), LocalDateTime.now()));
        List<OrderItem> savedItems = new ArrayList<>();
        for (int index = 0; index < placeOrderRequest.getPurchaseProduct().size(); index++) {
            PurchaseProductDTO purchase = placeOrderRequest.getPurchaseProduct().get(index);
            Inventory inventory = inventoryEntries.get(index);
            Product product = products.get(index);

            inventory.setStockLevel(inventory.getStockLevel() - purchase.getQuantity());
            inventoryRepository.save(inventory);

            OrderItem item = new OrderItem(order, product, purchase.getQuantity(), product.getPrice());
            savedItems.add(orderItemRepository.save(item));
        }
        order.setOrderItems(savedItems);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
