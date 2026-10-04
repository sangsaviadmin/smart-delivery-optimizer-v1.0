package com.logismart.algorithm;

import com.logismart.model.DeliveryOrder;
import com.logismart.model.Vehicle;
import com.logismart.model.ZoneDistance;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

@Component
public class RouteOptimizer {

    private static final double AVERAGE_SPEED_KMPH = 40.0;
    private static final double MINUTES_PER_HOUR = 60.0;
    private static final int MAX_PLANNING_DAYS = 100;

    public List<RoutePlan> optimize(
            List<DeliveryOrder> orders,
            List<Vehicle> vehicles,
            List<ZoneDistance> zoneDistances) {

        Map<String, Map<String, Double>> graph =
                buildGraph(zoneDistances);

        List<DeliveryOrder> remainingOrders =
                new ArrayList<>(orders);

        remainingOrders.sort(
                Comparator
                        .comparingInt(
                                (DeliveryOrder order) ->
                                        priorityRank(order.getPriority()))
                        .thenComparingInt(
                                order ->
                                        deliveryWindowRank(
                                                order.getDeliveryWindow()))
                        .thenComparing(
                                DeliveryOrder::getPackageWeightKg,
                                Comparator.reverseOrder())
        );

        List<RoutePlan> allRoutes =
                new ArrayList<>();

        int planningDay = 1;

        while (!remainingOrders.isEmpty()
                && planningDay <= MAX_PLANNING_DAYS) {

            List<RoutePlan> dayRoutes =
                    createDayRoutes(
                            remainingOrders,
                            vehicles,
                            graph,
                            planningDay
                    );

            if (dayRoutes.isEmpty()) {
                break;
            }

            allRoutes.addAll(dayRoutes);

            Set<String> allocatedIds =
                    new HashSet<>();

            for (RoutePlan route : dayRoutes) {
                for (DeliveryOrder order : route.getOrders()) {
                    allocatedIds.add(order.getOrderId());
                }
            }

            if (allocatedIds.isEmpty()) {
                break;
            }

            remainingOrders.removeIf(
                    order -> allocatedIds.contains(order.getOrderId())
            );

            planningDay++;
        }

        return allRoutes;
    }

    private List<RoutePlan> createDayRoutes(
            List<DeliveryOrder> remainingOrders,
            List<Vehicle> vehicles,
            Map<String, Map<String, Double>> graph,
            int planningDay) {

        List<RoutePlan> routes =
                new ArrayList<>();

        for (DeliveryOrder order : remainingOrders) {

            RoutePlan bestRoute = null;
            double bestIncrementalDistance =
                    Double.MAX_VALUE;

            for (RoutePlan route : routes) {

                if (!isCompatible(
                        order,
                        route.getVehicle())) {
                    continue;
                }

                RouteCandidate candidate =
                        evaluateInsertion(
                                order,
                                route,
                                graph
                        );

                if (!candidate.feasible) {
                    continue;
                }

                if (candidate.incrementalDistance
                        < bestIncrementalDistance) {

                    bestIncrementalDistance =
                            candidate.incrementalDistance;

                    bestRoute = route;
                }
            }

            if (bestRoute != null) {

                addOrderToRoute(
                        bestRoute,
                        order,
                        graph
                );

                continue;
            }

            Vehicle bestVehicle = null;
            double bestVehicleDistance =
                    Double.MAX_VALUE;

            for (Vehicle vehicle : vehicles) {

                if (!isCompatible(order, vehicle)) {
                    continue;
                }

                if (hasRouteForVehicle(
                        routes,
                        vehicle)) {
                    continue;
                }

                RoutePlan candidateRoute =
                        createInitialRoute(
                                vehicle,
                                planningDay
                        );

                RouteCandidate candidate =
                        evaluateInsertion(
                                order,
                                candidateRoute,
                                graph
                        );

                if (!candidate.feasible) {
                    continue;
                }

                if (candidate.incrementalDistance
                        < bestVehicleDistance) {

                    bestVehicleDistance =
                            candidate.incrementalDistance;

                    bestVehicle = vehicle;
                }
            }

            if (bestVehicle != null) {

                RoutePlan newRoute =
                        createInitialRoute(
                                bestVehicle,
                                planningDay
                        );

                addOrderToRoute(
                        newRoute,
                        order,
                        graph
                );

                routes.add(newRoute);
            }
        }

        closeDailyRoutes(routes);

        return routes;
    }

    private RoutePlan createInitialRoute(
            Vehicle vehicle,
            int planningDay) {

        RoutePlan route = new RoutePlan();

        route.setVehicle(vehicle);
        route.setPlanningDay(planningDay);
        route.setCurrentLocation(vehicle.getHub());

        return route;
    }

    private RouteCandidate evaluateInsertion(
            DeliveryOrder order,
            RoutePlan route,
            Map<String, Map<String, Double>> graph) {

        Vehicle vehicle = route.getVehicle();

        String currentLocation =
                route.getCurrentLocation();

        String destination =
                order.getDestinationZone();

        double currentToDestination =
                shortestPathDistance(
                        graph,
                        currentLocation,
                        destination
                );

        double destinationToHub =
                shortestPathDistance(
                        graph,
                        destination,
                        vehicle.getHub()
                );

        double currentToHub =
                shortestPathDistance(
                        graph,
                        currentLocation,
                        vehicle.getHub()
                );

        if (Double.isInfinite(currentToDestination)
                || Double.isInfinite(destinationToHub)
                || Double.isInfinite(currentToHub)) {

            return RouteCandidate.infeasible();
        }

        double incrementalDistance =
                currentToDestination
                        + destinationToHub
                        - currentToHub;

        double travelTime =
                distanceToMinutes(
                        currentToDestination
                );

        double destinationToHubTime =
                distanceToMinutes(
                        destinationToHub
                );

        double currentToHubTime =
                distanceToMinutes(
                        currentToHub
                );

        double incrementalTime =
                travelTime
                        + order.getServiceTimeMin()
                        + destinationToHubTime
                        - currentToHubTime;

        double newWeight =
                route.getTotalWeightKg()
                        + order.getPackageWeightKg();

        double newTime =
                route.getTotalTimeMin()
                        + incrementalTime;

        if (newWeight > vehicle.getCapacityKg()) {
            return RouteCandidate.infeasible();
        }

        if (newTime > vehicle.getMaxWorkingMinutes()) {
            return RouteCandidate.infeasible();
        }

        if (!isDeliveryWindowCompatible(
                order,
                route)) {
            return RouteCandidate.infeasible();
        }

        return new RouteCandidate(
                true,
                incrementalDistance,
                incrementalTime
        );
    }

    private void addOrderToRoute(
            RoutePlan route,
            DeliveryOrder order,
            Map<String, Map<String, Double>> graph) {

        Vehicle vehicle =
                route.getVehicle();

        String currentLocation =
                route.getCurrentLocation();

        String destination =
                order.getDestinationZone();

        double currentToDestination =
                shortestPathDistance(
                        graph,
                        currentLocation,
                        destination
                );

        double currentToHub =
                shortestPathDistance(
                        graph,
                        currentLocation,
                        vehicle.getHub()
                );

        if (!route.getOrders().isEmpty()) {

            route.setTotalDistanceKm(
                    route.getTotalDistanceKm()
                            - currentToHub
            );

            route.setTotalTimeMin(
                    route.getTotalTimeMin()
                            - distanceToMinutes(
                                    currentToHub
                            )
            );
        }

        route.getOrders().add(order);

        route.setTotalDistanceKm(
                route.getTotalDistanceKm()
                        + currentToDestination
        );

        route.setTotalWeightKg(
                route.getTotalWeightKg()
                        + order.getPackageWeightKg()
        );

        route.setTotalTimeMin(
                route.getTotalTimeMin()
                        + distanceToMinutes(
                                currentToDestination
                        )
                        + order.getServiceTimeMin()
        );

        double destinationToHub =
                shortestPathDistance(
                        graph,
                        destination,
                        vehicle.getHub()
                );

        route.setTotalDistanceKm(
                route.getTotalDistanceKm()
                        + destinationToHub
        );

        route.setTotalTimeMin(
                route.getTotalTimeMin()
                        + distanceToMinutes(
                                destinationToHub
                        )
        );

        route.setTotalCostInr(
                route.getTotalDistanceKm()
                        * vehicle.getCostPerKmInr()
        );

        route.setCurrentLocation(destination);
    }

    private boolean isDeliveryWindowCompatible(
            DeliveryOrder order,
            RoutePlan route) {

        if (route.getOrders().isEmpty()) {
            return true;
        }

        int newRank =
                deliveryWindowRank(
                        order.getDeliveryWindow()
                );

        DeliveryOrder lastOrder =
                route.getOrders()
                        .get(
                                route.getOrders().size() - 1
                        );

        int lastRank =
                deliveryWindowRank(
                        lastOrder.getDeliveryWindow()
                );

        if (newRank == 0 || lastRank == 0) {
            return true;
        }

        return newRank >= lastRank;
    }

    private boolean hasRouteForVehicle(
            List<RoutePlan> routes,
            Vehicle vehicle) {

        for (RoutePlan route : routes) {

            if (route.getVehicle() != null
                    && route.getVehicle()
                    .getVehicleId()
                    .equals(vehicle.getVehicleId())) {

                return true;
            }
        }

        return false;
    }

    private boolean isCompatible(
            DeliveryOrder order,
            Vehicle vehicle) {

        if (vehicle == null
                || vehicle.getAvailability() == null
                || !vehicle.getAvailability()
                .equalsIgnoreCase("Available")) {
            return false;
        }

        if (order.getOriginHub() == null
                || vehicle.getHub() == null
                || !order.getOriginHub()
                .equalsIgnoreCase(vehicle.getHub())) {
            return false;
        }

        if (order.getRequiredVehicleType() == null
                || vehicle.getVehicleType() == null
                || !order.getRequiredVehicleType()
                .equalsIgnoreCase(vehicle.getVehicleType())) {
            return false;
        }

        return order.getPackageWeightKg()
                <= vehicle.getCapacityKg();
    }

    private void closeDailyRoutes(
            List<RoutePlan> routes) {

        for (RoutePlan route : routes) {

            if (route.getVehicle() != null) {

                route.setCurrentLocation(
                        route.getVehicle().getHub()
                );
            }
        }
    }

    private double distanceToMinutes(
            double distanceKm) {

        return distanceKm
                / AVERAGE_SPEED_KMPH
                * MINUTES_PER_HOUR;
    }

    private int priorityRank(
            String priority) {

        if (priority == null) {
            return 4;
        }

        return switch (
                priority.trim().toLowerCase()) {

            case "high" -> 1;
            case "medium" -> 2;
            case "low" -> 3;
            default -> 4;
        };
    }

    private int deliveryWindowRank(
            String window) {

        if (window == null
                || window.isBlank()
                || window.equalsIgnoreCase("Flexible")) {
            return 0;
        }

        return switch (window.trim()) {

            case "08:00-11:00" -> 1;
            case "11:00-14:00" -> 2;
            case "14:00-17:00" -> 3;
            default -> 0;
        };
    }

    private Map<String, Map<String, Double>>
    buildGraph(
            List<ZoneDistance> zoneDistances) {

        Map<String, Map<String, Double>> graph =
                new HashMap<>();

        for (ZoneDistance distance :
                zoneDistances) {

            graph
                    .computeIfAbsent(
                            distance.getFromLocation(),
                            key -> new HashMap<>()
                    )
                    .put(
                            distance.getToLocation(),
                            distance.getDistanceKm()
                    );
        }

        return graph;
    }

    private double shortestPathDistance(
            Map<String, Map<String, Double>> graph,
            String start,
            String target) {

        if (start == null || target == null) {
            return Double.POSITIVE_INFINITY;
        }

        if (start.equalsIgnoreCase(target)) {
            return 0.0;
        }

        PriorityQueue<NodeDistance> queue =
                new PriorityQueue<>(
                        Comparator.comparingDouble(
                                NodeDistance::getDistance
                        )
                );

        Map<String, Double> distances =
                new HashMap<>();

        distances.put(start, 0.0);

        queue.offer(
                new NodeDistance(start, 0.0)
        );

        while (!queue.isEmpty()) {

            NodeDistance current =
                    queue.poll();

            String node =
                    current.getNode();

            double distance =
                    current.getDistance();

            if (distance >
                    distances.getOrDefault(
                            node,
                            Double.POSITIVE_INFINITY)) {
                continue;
            }

            if (node.equalsIgnoreCase(target)) {
                return distance;
            }

            for (Map.Entry<String, Double> edge :
                    graph.getOrDefault(
                            node,
                            Map.of()
                    ).entrySet()) {

                double newDistance =
                        distance + edge.getValue();

                if (newDistance <
                        distances.getOrDefault(
                                edge.getKey(),
                                Double.POSITIVE_INFINITY)) {

                    distances.put(
                            edge.getKey(),
                            newDistance
                    );

                    queue.offer(
                            new NodeDistance(
                                    edge.getKey(),
                                    newDistance
                            )
                    );
                }
            }
        }

        return Double.POSITIVE_INFINITY;
    }

    private static class NodeDistance {

        private final String node;
        private final double distance;

        NodeDistance(
                String node,
                double distance) {

            this.node = node;
            this.distance = distance;
        }

        String getNode() {
            return node;
        }

        double getDistance() {
            return distance;
        }
    }

    private static class RouteCandidate {

        private final boolean feasible;
        private final double incrementalDistance;
        private final double incrementalTime;

        RouteCandidate(
                boolean feasible,
                double incrementalDistance,
                double incrementalTime) {

            this.feasible = feasible;
            this.incrementalDistance =
                    incrementalDistance;
            this.incrementalTime =
                    incrementalTime;
        }

        static RouteCandidate infeasible() {

            return new RouteCandidate(
                    false,
                    Double.MAX_VALUE,
                    Double.MAX_VALUE
            );
        }
    }
}