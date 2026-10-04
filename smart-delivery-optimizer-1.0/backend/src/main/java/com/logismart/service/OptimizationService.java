package com.logismart.service;

import com.logismart.algorithm.RouteOptimizer;
import com.logismart.algorithm.RoutePlan;
import com.logismart.model.DeliveryOrder;
import com.logismart.model.KpiComparison;
import com.logismart.model.OptimizationSummary;
import com.logismart.model.Vehicle;
import com.logismart.model.ZoneDistance;
import com.logismart.repository.DeliveryOrderRepository;
import com.logismart.repository.VehicleRepository;
import com.logismart.repository.ZoneDistanceRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OptimizationService {

    private final DeliveryOrderRepository deliveryOrderRepository;
    private final VehicleRepository vehicleRepository;
    private final ZoneDistanceRepository zoneDistanceRepository;
    private final RouteOptimizer routeOptimizer;

    public OptimizationService(
            DeliveryOrderRepository deliveryOrderRepository,
            VehicleRepository vehicleRepository,
            ZoneDistanceRepository zoneDistanceRepository,
            RouteOptimizer routeOptimizer) {

        this.deliveryOrderRepository = deliveryOrderRepository;
        this.vehicleRepository = vehicleRepository;
        this.zoneDistanceRepository = zoneDistanceRepository;
        this.routeOptimizer = routeOptimizer;
    }

    public List<RoutePlan> optimizeRoutes() {

        List<DeliveryOrder> orders =
                deliveryOrderRepository.findAll();

        List<Vehicle> vehicles =
                vehicleRepository.findAll();

        List<ZoneDistance> zoneDistances =
                zoneDistanceRepository.findAll();

        List<RoutePlan> routes =
                routeOptimizer.optimize(
                        orders,
                        vehicles,
                        zoneDistances
                );

        Set<String> allocatedOrderIds =
                new HashSet<>();

        for (RoutePlan route : routes) {
            for (DeliveryOrder order : route.getOrders()) {
                allocatedOrderIds.add(order.getOrderId());
            }
        }

        for (DeliveryOrder order : orders) {

            if (!allocatedOrderIds.contains(
                    order.getOrderId())) {

                order.setAllocationReason(
                        determineUnallocatedReason(
                                order,
                                vehicles,
                                zoneDistances
                        )
                );
            }
        }

        return routes;
    }

    public List<DeliveryOrder> getUnallocatedOrders() {

        List<DeliveryOrder> orders =
                deliveryOrderRepository.findAll();

        List<Vehicle> vehicles =
                vehicleRepository.findAll();

        List<ZoneDistance> zoneDistances =
                zoneDistanceRepository.findAll();

        List<RoutePlan> routes =
                optimizeRoutes();

        Set<String> allocatedOrderIds =
                new HashSet<>();

        for (RoutePlan route : routes) {
            for (DeliveryOrder order : route.getOrders()) {
                allocatedOrderIds.add(order.getOrderId());
            }
        }

        List<DeliveryOrder> unallocated =
                new ArrayList<>();

        for (DeliveryOrder order : orders) {

            if (!allocatedOrderIds.contains(
                    order.getOrderId())) {

                order.setAllocationReason(
                        determineUnallocatedReason(
                                order,
                                vehicles,
                                zoneDistances
                        )
                );

                unallocated.add(order);
            }
        }

        return unallocated;
    }

    public Map<String, Integer> getUnallocatedReasonCounts() {

        List<DeliveryOrder> unallocated =
                getUnallocatedOrders();

        Map<String, Integer> reasonCounts =
                new HashMap<>();

        for (DeliveryOrder order : unallocated) {

            String reason =
                    order.getAllocationReason();

            reasonCounts.merge(
                    reason,
                    1,
                    Integer::sum
            );
        }

        return reasonCounts;
    }

    public OptimizationSummary getOptimizationSummary() {

        List<RoutePlan> routes =
                optimizeRoutes();

        OptimizationSummary summary =
                new OptimizationSummary();

        int ordersAllocated =
                routes.stream()
                        .mapToInt(route ->
                                route.getOrders().size())
                        .sum();

        double totalDistance =
                routes.stream()
                        .mapToDouble(
                                RoutePlan::getTotalDistanceKm)
                        .sum();

        double totalWeight =
                routes.stream()
                        .mapToDouble(
                                RoutePlan::getTotalWeightKg)
                        .sum();

        double totalTime =
                routes.stream()
                        .mapToDouble(
                                RoutePlan::getTotalTimeMin)
                        .sum();

        double totalCost =
                routes.stream()
                        .mapToDouble(
                                RoutePlan::getTotalCostInr)
                        .sum();

        int vehiclesUsed =
                (int) routes.stream()
                        .map(route ->
                                route.getVehicle()
                                        .getVehicleId())
                        .distinct()
                        .count();

        int planningDays =
                routes.stream()
                        .mapToInt(
                                RoutePlan::getPlanningDay)
                        .max()
                        .orElse(0);

        double averageVehicleUtilization =
                routes.stream()
                        .filter(route ->
                                route.getVehicle() != null
                                && route.getVehicle()
                                .getMaxWorkingMinutes() > 0)
                        .mapToDouble(route ->
                                (route.getTotalTimeMin()
                                / route.getVehicle()
                                .getMaxWorkingMinutes())
                                * 100.0)
                        .average()
                        .orElse(0.0);

        double averagePlannedTimePerOrder =
                ordersAllocated == 0
                        ? 0.0
                        : totalTime / ordersAllocated;

        summary.setPlanningDays(planningDays);
        summary.setVehiclesUsed(vehiclesUsed);
        summary.setOrdersAllocated(ordersAllocated);
        summary.setTotalDistanceKm(totalDistance);
        summary.setTotalWeightKg(totalWeight);
        summary.setTotalTimeMin(totalTime);
        summary.setTotalCostInr(totalCost);

        summary.setAverageOrdersPerDay(
                planningDays == 0
                        ? 0.0
                        : (double) ordersAllocated / planningDays
        );

        summary.setAverageDistancePerDayKm(
                planningDays == 0
                        ? 0.0
                        : totalDistance / planningDays
        );

        summary.setAverageTimePerDayMin(
                planningDays == 0
                        ? 0.0
                        : totalTime / planningDays
        );

        summary.setAverageCostPerDayInr(
                planningDays == 0
                        ? 0.0
                        : totalCost / planningDays
        );

        summary.setAverageVehicleUtilizationPercent(
                averageVehicleUtilization
        );

        summary.setAveragePlannedTimePerOrderMin(
                averagePlannedTimePerOrder
        );

        /*
         * BASELINE
         *
         * Distance, time and delay come directly
         * from the supplied source baseline fields.
         *
         * Baseline cost and utilization are calculated
         * using the constructed baseline allocation model.
         */

        List<DeliveryOrder> baselineOrders =
                deliveryOrderRepository.findAll();

        List<Vehicle> baselineVehicles =
                vehicleRepository.findAll();

        int baselineOrderCount =
                baselineOrders.size();

        double baselineDistance =
                baselineOrders.stream()
                        .mapToDouble(
                                DeliveryOrder::
                                        getExistingPlannedDistanceKm)
                        .sum();

        double baselineTime =
                baselineOrders.stream()
                        .mapToDouble(
                                DeliveryOrder::
                                        getExistingEstimatedTimeMin)
                        .sum();

        double baselineDelay =
                baselineOrders.stream()
                        .mapToDouble(
                                DeliveryOrder::getDelayMin)
                        .sum();

        long baselineOnTimeOrders =
                baselineOrders.stream()
                        .filter(order ->
                                order.getDelayMin() <= 0)
                        .count();

        BaselinePlanningResult baseline =
                calculateBaselinePlanning(
                        baselineOrders,
                        baselineVehicles
                );

        summary.setBaselinePlanningDays(
                baseline.planningDays
        );

        summary.setBaselineVehiclesUsed(
                baseline.vehiclesUsed
        );

        summary.setBaselineTrips(
                baseline.trips
        );

        summary.setBaselineOrders(
                baselineOrderCount
        );

        summary.setBaselineOrdersPerDay(
                baseline.planningDays == 0
                        ? 0.0
                        : (double) baselineOrderCount
                        / baseline.planningDays
        );

        summary.setBaselineDistanceKm(
                baselineDistance
        );

        summary.setBaselineTimeMin(
                baselineTime
        );

        summary.setBaselineDelayMin(
                baselineDelay
        );

        summary.setBaselineAverageDelayMin(
                baselineOrderCount == 0
                        ? 0.0
                        : baselineDelay
                        / baselineOrderCount
        );

        summary.setBaselineOnTimeRatePercent(
                baselineOrderCount == 0
                        ? 0.0
                        : ((double) baselineOnTimeOrders
                        / baselineOrderCount) * 100.0
        );

        summary.setBaselineTotalCostInr(
                baseline.totalCostInr
        );

        summary.setBaselineAverageVehicleUtilizationPercent(
                baseline.averageVehicleUtilizationPercent
        );

        return summary;
    }

    private BaselinePlanningResult calculateBaselinePlanning(
            List<DeliveryOrder> orders,
            List<Vehicle> vehicles) {

        List<Vehicle> availableVehicles =
                vehicles.stream()
                        .filter(vehicle ->
                                vehicle.getAvailability() != null
                                && vehicle.getAvailability()
                                .equalsIgnoreCase("Available"))
                        .toList();

        if (availableVehicles.isEmpty()
                || orders.isEmpty()) {

            return new BaselinePlanningResult(
                    0,
                    0,
                    0,
                    0.0,
                    0.0
            );
        }

        /*
         * Workload by vehicle-day.
         *
         * IMPORTANT:
         * Keep the original baseline planning model.
         * Baseline workload uses the supplied
         * Existing Estimated Time plus Service Time.
         */
        Map<String, Double> vehicleDayWorkload =
                new HashMap<>();

        /*
         * Cost by vehicle-day.
         */
        Map<String, Double> vehicleDayCost =
                new HashMap<>();

        Set<String> vehiclesUsed =
                new HashSet<>();

        /*
         * One trip = one unique vehicle-day.
         */
        Set<String> baselineTripKeys =
                new HashSet<>();

        int planningDays = 0;

        List<DeliveryOrder> sortedOrders =
                new ArrayList<>(orders);

        sortedOrders.sort(
                Comparator
                        .comparingInt(
                                this::priorityRank)
                        .reversed()
                        .thenComparing(
                                DeliveryOrder::
                                        getPackageWeightKg,
                                Comparator.reverseOrder()
                        )
        );

        for (DeliveryOrder order : sortedOrders) {

            int assignedDay = 0;
            Vehicle assignedVehicle = null;

            for (int day = 1; day <= 100; day++) {

                for (Vehicle vehicle :
                        availableVehicles) {

                    if (!isBaselineCompatible(
                            order,
                            vehicle)) {
                        continue;
                    }

                    String key =
                            vehicle.getVehicleId()
                            + "|"
                            + day;

                    double currentWorkload =
                            vehicleDayWorkload
                                    .getOrDefault(key, 0.0);

                    /*
                     * Original baseline workload:
                     *
                     * Existing estimated time
                     * + service time.
                     */
                    double orderWorkload =
                            order.getExistingEstimatedTimeMin()
                            + order.getServiceTimeMin();

                    if (currentWorkload
                            + orderWorkload
                            <= vehicle.getMaxWorkingMinutes()) {

                        assignedDay = day;
                        assignedVehicle = vehicle;

                        vehicleDayWorkload.put(
                                key,
                                currentWorkload
                                + orderWorkload
                        );

                        /*
                         * Baseline cost:
                         * source planned distance multiplied
                         * by the assigned compatible vehicle's
                         * cost per kilometre.
                         */
                        double orderCost =
                                order.getExistingPlannedDistanceKm()
                                * vehicle.getCostPerKmInr();

                        vehicleDayCost.merge(
                                key,
                                orderCost,
                                Double::sum
                        );

                        break;
                    }
                }

                if (assignedVehicle != null) {
                    break;
                }
            }

            if (assignedVehicle != null) {

                planningDays =
                        Math.max(
                                planningDays,
                                assignedDay
                        );

                String key =
                        assignedVehicle.getVehicleId()
                        + "|"
                        + assignedDay;

                baselineTripKeys.add(key);

                vehiclesUsed.add(
                        assignedVehicle.getVehicleId()
                );
            }
        }

        double totalBaselineCost =
                vehicleDayCost.values().stream()
                        .mapToDouble(Double::doubleValue)
                        .sum();

        /*
         * Baseline utilization:
         *
         * workload / maximum working minutes
         *
         * averaged across used vehicle-days.
         */
        double averageBaselineUtilization =
                vehicleDayWorkload.entrySet().stream()
                        .filter(entry -> {

                            String[] parts =
                                    entry.getKey()
                                            .split("\\|");

                            if (parts.length != 2) {
                                return false;
                            }

                            String vehicleId =
                                    parts[0];

                            return availableVehicles.stream()
                                    .anyMatch(vehicle ->
                                            vehicle.getVehicleId()
                                                    .equals(vehicleId)
                                            && vehicle
                                                    .getMaxWorkingMinutes()
                                                    > 0);
                        })
                        .mapToDouble(entry -> {

                            String[] parts =
                                    entry.getKey()
                                            .split("\\|");

                            String vehicleId =
                                    parts[0];

                            int maxMinutes =
                                    availableVehicles.stream()
                                            .filter(vehicle ->
                                                    vehicle
                                                            .getVehicleId()
                                                            .equals(
                                                                    vehicleId))
                                            .mapToInt(
                                                    Vehicle::
                                                            getMaxWorkingMinutes)
                                            .findFirst()
                                            .orElse(0);

                            if (maxMinutes == 0) {
                                return 0.0;
                            }

                            return (
                                    entry.getValue()
                                    / maxMinutes
                            ) * 100.0;
                        })
                        .average()
                        .orElse(0.0);

        return new BaselinePlanningResult(
                planningDays,
                vehiclesUsed.size(),
                baselineTripKeys.size(),
                totalBaselineCost,
                averageBaselineUtilization
        );
    }

    private boolean isBaselineCompatible(
            DeliveryOrder order,
            Vehicle vehicle) {

        if (order.getOriginHub() == null
                || vehicle.getHub() == null
                || !order.getOriginHub()
                .equalsIgnoreCase(
                        vehicle.getHub())) {

            return false;
        }

        if (order.getRequiredVehicleType() == null
                || vehicle.getVehicleType() == null
                || !order.getRequiredVehicleType()
                .equalsIgnoreCase(
                        vehicle.getVehicleType())) {

            return false;
        }

        return order.getPackageWeightKg()
                <= vehicle.getCapacityKg();
    }

    private int priorityRank(
            DeliveryOrder order) {

        if (order.getPriority() == null) {
            return 0;
        }

        return switch (
                order.getPriority().toLowerCase()) {

            case "high" -> 3;
            case "medium" -> 2;
            case "low" -> 1;
            default -> 0;
        };
    }

    private static class BaselinePlanningResult {

        private final int planningDays;
        private final int vehiclesUsed;
        private final int trips;
        private final double totalCostInr;
        private final double averageVehicleUtilizationPercent;

        private BaselinePlanningResult(
                int planningDays,
                int vehiclesUsed,
                int trips,
                double totalCostInr,
                double averageVehicleUtilizationPercent) {

            this.planningDays = planningDays;
            this.vehiclesUsed = vehiclesUsed;
            this.trips = trips;
            this.totalCostInr = totalCostInr;
            this.averageVehicleUtilizationPercent =
                    averageVehicleUtilizationPercent;
        }
    }

    public KpiComparison getKpiComparison() {

        List<DeliveryOrder> orders =
                deliveryOrderRepository.findAll();

        List<Vehicle> vehicles =
                vehicleRepository.findAll();

        List<RoutePlan> routes =
                optimizeRoutes();

        KpiComparison comparison =
                new KpiComparison();

        int baselineOrders =
                orders.size();

        double baselineDistance =
                orders.stream()
                        .mapToDouble(
                                DeliveryOrder::
                                        getExistingPlannedDistanceKm)
                        .sum();

        double baselineTime =
                orders.stream()
                        .mapToDouble(
                                DeliveryOrder::
                                        getExistingEstimatedTimeMin)
                        .sum();

        double baselineDelay =
                orders.stream()
                        .mapToDouble(
                                DeliveryOrder::getDelayMin)
                        .sum();

        int delayedOrders =
                (int) orders.stream()
                        .filter(order ->
                                order.getDelayMin() > 0)
                        .count();

        int onTimeOrders =
                baselineOrders - delayedOrders;

        int atRiskOrders =
                (int) orders.stream()
                        .filter(order ->
                                order.getDelayMin() > 0
                                && order.getDelayMin() <= 30)
                        .count();

        int optimizedOrders =
                routes.stream()
                        .mapToInt(route ->
                                route.getOrders().size())
                        .sum();

        double optimizedDistance =
                routes.stream()
                        .mapToDouble(
                                RoutePlan::getTotalDistanceKm)
                        .sum();

        double optimizedTime =
                routes.stream()
                        .mapToDouble(
                                RoutePlan::getTotalTimeMin)
                        .sum();

        double optimizedCost =
                routes.stream()
                        .mapToDouble(
                                RoutePlan::getTotalCostInr)
                        .sum();

        int vehiclesUsed =
                (int) routes.stream()
                        .map(route ->
                                route.getVehicle()
                                        .getVehicleId())
                        .distinct()
                        .count();

        int planningDays =
                routes.stream()
                        .mapToInt(
                                RoutePlan::getPlanningDay)
                        .max()
                        .orElse(0);

        double distanceReductionPercent =
                baselineDistance == 0
                        ? 0.0
                        : ((baselineDistance
                        - optimizedDistance)
                        / baselineDistance) * 100.0;

        double timeReductionPercent =
                baselineTime == 0
                        ? 0.0
                        : ((baselineTime
                        - optimizedTime)
                        / baselineTime) * 100.0;

        double baselineOnTimeRate =
                baselineOrders == 0
                        ? 0.0
                        : ((double) onTimeOrders
                        / baselineOrders) * 100.0;

        double optimizedAllocationRate =
                baselineOrders == 0
                        ? 0.0
                        : ((double) optimizedOrders
                        / baselineOrders) * 100.0;

        double averageBaselineDelay =
                baselineOrders == 0
                        ? 0.0
                        : baselineDelay / baselineOrders;

        int availableVehicles =
                (int) vehicles.stream()
                        .filter(vehicle ->
                                vehicle.getAvailability() != null
                                && vehicle.getAvailability()
                                .equalsIgnoreCase("Available"))
                        .count();

        comparison.setBaselineOrders(
                baselineOrders
        );

        comparison.setOptimizedOrders(
                optimizedOrders
        );

        comparison.setBaselineDistanceKm(
                baselineDistance
        );

        comparison.setOptimizedDistanceKm(
                optimizedDistance
        );

        comparison.setDistanceReductionPercent(
                distanceReductionPercent
        );

        comparison.setBaselineTimeMin(
                baselineTime
        );

        comparison.setOptimizedTimeMin(
                optimizedTime
        );

        comparison.setTimeReductionPercent(
                timeReductionPercent
        );

        comparison.setBaselineDelayMin(
                baselineDelay
        );

        comparison.setAverageBaselineDelayMin(
                averageBaselineDelay
        );

        comparison.setBaselineOnTimeOrders(
                onTimeOrders
        );

        comparison.setBaselineDelayedOrders(
                delayedOrders
        );

        comparison.setBaselineAtRiskOrders(
                atRiskOrders
        );

        comparison.setBaselineOnTimeRatePercent(
                baselineOnTimeRate
        );

        comparison.setOptimizedAllocationRatePercent(
                optimizedAllocationRate
        );

        comparison.setAvailableVehicles(
                availableVehicles
        );

        comparison.setVehiclesUsed(
                vehiclesUsed
        );

        comparison.setPlanningDays(
                planningDays
        );

        comparison.setOptimizedCostInr(
                optimizedCost
        );

        return comparison;
    }

    public List<Map<String, Object>>
    getDailyFleetUtilization() {

        List<RoutePlan> routes =
                optimizeRoutes();

        Map<Integer, Map<String, Object>>
                dailyData =
                new HashMap<>();

        for (RoutePlan route : routes) {

            int day =
                    route.getPlanningDay();

            Map<String, Object> data =
                    dailyData.computeIfAbsent(
                            day,
                            key -> {

                                Map<String, Object>
                                        result =
                                        new HashMap<>();

                                result.put(
                                        "planningDay",
                                        key
                                );

                                result.put(
                                        "vehiclesUsed",
                                        0
                                );

                                result.put(
                                        "ordersAllocated",
                                        0
                                );

                                result.put(
                                        "distanceKm",
                                        0.0
                                );

                                result.put(
                                        "timeMin",
                                        0.0
                                );

                                result.put(
                                        "costInr",
                                        0.0
                                );

                                result.put(
                                        "weightKg",
                                        0.0
                                );

                                return result;
                            }
                    );

            data.put(
                    "vehiclesUsed",
                    (Integer) data.get(
                            "vehiclesUsed"
                    ) + 1
            );

            data.put(
                    "ordersAllocated",
                    (Integer) data.get(
                            "ordersAllocated"
                    ) + route.getOrders().size()
            );

            data.put(
                    "distanceKm",
                    (Double) data.get(
                            "distanceKm"
                    ) + route.getTotalDistanceKm()
            );

            data.put(
                    "timeMin",
                    (Double) data.get(
                            "timeMin"
                    ) + route.getTotalTimeMin()
            );

            data.put(
                    "costInr",
                    (Double) data.get(
                            "costInr"
                    ) + route.getTotalCostInr()
            );

            data.put(
                    "weightKg",
                    (Double) data.get(
                            "weightKg"
                    ) + route.getTotalWeightKg()
            );
        }

        List<Map<String, Object>> results =
                new ArrayList<>(
                        dailyData.values()
                );

        results.sort(
                Comparator.comparingInt(
                        (Map<String, Object> item) ->
                                (Integer) item.get(
                                        "planningDay")
                )
        );

        return results;
    }

    public List<Map<String, Object>>
    getDailyVehicleTypeUtilization() {

        List<RoutePlan> routes =
                optimizeRoutes();

        Map<String, Map<String, Object>>
                typeData =
                new HashMap<>();

        for (RoutePlan route : routes) {

            String key =
                    route.getPlanningDay()
                    + "|"
                    + route.getVehicle()
                    .getVehicleType();

            Map<String, Object> data =
                    typeData.computeIfAbsent(
                            key,
                            ignored -> {

                                Map<String, Object>
                                        result =
                                        new HashMap<>();

                                result.put(
                                        "planningDay",
                                        route.getPlanningDay()
                                );

                                result.put(
                                        "vehicleType",
                                        route.getVehicle()
                                                .getVehicleType()
                                );

                                result.put(
                                        "vehiclesUsed",
                                        0
                                );

                                result.put(
                                        "ordersAllocated",
                                        0
                                );

                                result.put(
                                        "distanceKm",
                                        0.0
                                );

                                result.put(
                                        "timeMin",
                                        0.0
                                );

                                result.put(
                                        "weightKg",
                                        0.0
                                );

                                return result;
                            }
                    );

            data.put(
                    "vehiclesUsed",
                    (Integer) data.get(
                            "vehiclesUsed"
                    ) + 1
            );

            data.put(
                    "ordersAllocated",
                    (Integer) data.get(
                            "ordersAllocated"
                    ) + route.getOrders().size()
            );

            data.put(
                    "distanceKm",
                    (Double) data.get(
                            "distanceKm"
                    ) + route.getTotalDistanceKm()
            );

            data.put(
                    "timeMin",
                    (Double) data.get(
                            "timeMin"
                    ) + route.getTotalTimeMin()
            );

            data.put(
                    "weightKg",
                    (Double) data.get(
                            "weightKg"
                    ) + route.getTotalWeightKg()
            );
        }

        List<Map<String, Object>> results =
                new ArrayList<>(
                        typeData.values()
                );

        results.sort(
                Comparator
                        .<Map<String, Object>>
                        comparingInt(
                                item ->
                                        (Integer) item.get(
                                                "planningDay")
                        )
                        .thenComparing(
                                item ->
                                        (String) item.get(
                                                "vehicleType")
                        )
        );

        return results;
    }

    private String determineUnallocatedReason(
            DeliveryOrder order,
            List<Vehicle> vehicles,
            List<ZoneDistance> zoneDistances) {

        boolean availableCompatibleVehicle =
                false;

        boolean capacityCompatibleVehicle =
                false;

        for (Vehicle vehicle : vehicles) {

            if (vehicle.getAvailability() == null
                    || !vehicle.getAvailability()
                    .equalsIgnoreCase("Available")) {

                continue;
            }

            if (order.getOriginHub() == null
                    || vehicle.getHub() == null
                    || !order.getOriginHub()
                    .equalsIgnoreCase(
                            vehicle.getHub())) {

                continue;
            }

            if (order.getRequiredVehicleType() == null
                    || vehicle.getVehicleType() == null
                    || !order.getRequiredVehicleType()
                    .equalsIgnoreCase(
                            vehicle.getVehicleType())) {

                continue;
            }

            availableCompatibleVehicle =
                    true;

            if (order.getPackageWeightKg()
                    <= vehicle.getCapacityKg()) {

                capacityCompatibleVehicle =
                        true;

                break;
            }
        }

        if (!availableCompatibleVehicle) {
            return "No available vehicle matching hub and vehicle type";
        }

        if (!capacityCompatibleVehicle) {
            return "Package weight exceeds compatible vehicle capacity";
        }

        return "No feasible route within vehicle working-time or delivery-window constraints";
    }
}