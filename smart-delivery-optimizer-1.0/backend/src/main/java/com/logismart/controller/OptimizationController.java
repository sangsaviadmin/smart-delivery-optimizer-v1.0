package com.logismart.controller;

import com.logismart.algorithm.RoutePlan;
import com.logismart.model.DeliveryOrder;
import com.logismart.model.OptimizationSummary;
import com.logismart.service.OptimizationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/optimization")
public class OptimizationController {

    private final OptimizationService optimizationService;

    public OptimizationController(
            OptimizationService optimizationService) {

        this.optimizationService =
                optimizationService;
    }

    @GetMapping("/routes")
    public List<RoutePlan> getRoutes() {

        return optimizationService.optimizeRoutes();
    }

    @GetMapping("/summary")
    public OptimizationSummary getSummary() {

        return optimizationService.getOptimizationSummary();
    }

    @GetMapping("/unallocated")
    public List<DeliveryOrder> getUnallocatedOrders() {

        return optimizationService
                .getUnallocatedOrders();
    }

    @GetMapping("/unallocated/count")
    public int getUnallocatedCount() {

        return optimizationService
                .getUnallocatedOrders()
                .size();
    }

    @GetMapping("/unallocated/reasons")
    public Map<String, Integer> getUnallocatedReasons() {

        return optimizationService
                .getUnallocatedReasonCounts();
    }

    @GetMapping("/daily-utilization")
    public List<Map<String, Object>>
    getDailyFleetUtilization() {

        return optimizationService
                .getDailyFleetUtilization();
    }

    @GetMapping("/daily-type-utilization")
    public List<Map<String, Object>>
    getDailyVehicleTypeUtilization() {

        return optimizationService
                .getDailyVehicleTypeUtilization();
    }
}