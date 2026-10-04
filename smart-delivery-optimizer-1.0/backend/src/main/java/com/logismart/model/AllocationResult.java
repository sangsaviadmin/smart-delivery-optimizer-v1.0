package com.logismart.model;

import java.util.ArrayList;
import java.util.List;

public class AllocationResult {

    private List<DeliveryOrder> allocatedOrders = new ArrayList<>();
    private List<DeliveryOrder> unallocatedOrders = new ArrayList<>();
    private List<String> unallocatedReasons = new ArrayList<>();

    public List<DeliveryOrder> getAllocatedOrders() {
        return allocatedOrders;
    }

    public void setAllocatedOrders(List<DeliveryOrder> allocatedOrders) {
        this.allocatedOrders = allocatedOrders;
    }

    public List<DeliveryOrder> getUnallocatedOrders() {
        return unallocatedOrders;
    }

    public void setUnallocatedOrders(List<DeliveryOrder> unallocatedOrders) {
        this.unallocatedOrders = unallocatedOrders;
    }

    public List<String> getUnallocatedReasons() {
        return unallocatedReasons;
    }

    public void setUnallocatedReasons(List<String> unallocatedReasons) {
        this.unallocatedReasons = unallocatedReasons;
    }
}