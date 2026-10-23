package com.project.code.Controller;

import com.project.code.Model.PlaceOrderRequestDTO;
import com.project.code.Model.Store;
import com.project.code.Repo.StoreRepository;
import com.project.code.Service.OrderService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/store")
public class StoreController {
    private final StoreRepository storeRepository;
    private final OrderService orderService;

    public StoreController(StoreRepository storeRepository, OrderService orderService) {
        this.storeRepository = storeRepository;
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> addStore(@Valid @RequestBody Store store) {
        Store saved = storeRepository.save(store);
        return Map.of("message", "Store created successfully with ID " + saved.getId());
    }

    @GetMapping("validate/{storeId}")
    public boolean validateStore(@PathVariable Long storeId) {
        return storeRepository.existsById(storeId);
    }

    @PostMapping("/placeOrder")
    public Map<String, String> placeOrder(@Valid @RequestBody PlaceOrderRequestDTO request) {
        try {
            orderService.saveOrder(request);
            return Map.of("message", "Order placed successfully");
        } catch (IllegalArgumentException exception) {
            return Map.of("Error", exception.getMessage());
        }
    }
}
