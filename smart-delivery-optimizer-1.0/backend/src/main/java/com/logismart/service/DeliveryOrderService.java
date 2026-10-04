package com.logismart.service;

import com.logismart.model.DeliveryOrder;
import com.logismart.repository.DeliveryOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliveryOrderService {

    private final DeliveryOrderRepository repository;

    public DeliveryOrderService(DeliveryOrderRepository repository) {
        this.repository = repository;
    }

    public List<DeliveryOrder> getAllOrders() {
        return repository.findAll();
    }
}