package com.logismart.algorithm;

import com.logismart.model.DeliveryOrder;
import com.logismart.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class RoutePlan {

    private Vehicle vehicle;
    private int planningDay;
    private List<DeliveryOrder> orders = new ArrayList<>();

    private String currentLocation;

    private double totalDistanceKm;
    private double totalWeightKg;
    private double totalTimeMin;
    private double totalCostInr;

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public int getPlanningDay() {
        return planningDay;
    }

    public void setPlanningDay(int planningDay) {
        this.planningDay = planningDay;
    }

    public List<DeliveryOrder> getOrders() {
        return orders;
    }

    public void setOrders(List<DeliveryOrder> orders) {
        this.orders = orders;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public double getTotalWeightKg() {
        return totalWeightKg;
    }

    public void setTotalWeightKg(double totalWeightKg) {
        this.totalWeightKg = totalWeightKg;
    }

    public double getTotalTimeMin() {
        return totalTimeMin;
    }

    public void setTotalTimeMin(double totalTimeMin) {
        this.totalTimeMin = totalTimeMin;
    }

    public double getTotalCostInr() {
        return totalCostInr;
    }

    public void setTotalCostInr(double totalCostInr) {
        this.totalCostInr = totalCostInr;
    }
}