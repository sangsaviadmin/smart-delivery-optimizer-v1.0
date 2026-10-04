package com.logismart.model;

public class KpiComparison {

    private int baselineOrders;
    private int optimizedOrders;

    private double baselineDistanceKm;
    private double optimizedDistanceKm;
    private double distanceReductionPercent;

    private double baselineTimeMin;
    private double optimizedTimeMin;
    private double timeReductionPercent;

    private double baselineDelayMin;
    private double averageBaselineDelayMin;

    private int baselineOnTimeOrders;
    private int baselineDelayedOrders;
    private int baselineAtRiskOrders;

    private double baselineOnTimeRatePercent;
    private double optimizedAllocationRatePercent;

    private int availableVehicles;
    private int vehiclesUsed;

    private int planningDays;

    private double optimizedCostInr;

    public int getBaselineOrders() {
        return baselineOrders;
    }

    public void setBaselineOrders(int baselineOrders) {
        this.baselineOrders = baselineOrders;
    }

    public int getOptimizedOrders() {
        return optimizedOrders;
    }

    public void setOptimizedOrders(int optimizedOrders) {
        this.optimizedOrders = optimizedOrders;
    }

    public double getBaselineDistanceKm() {
        return baselineDistanceKm;
    }

    public void setBaselineDistanceKm(double baselineDistanceKm) {
        this.baselineDistanceKm = baselineDistanceKm;
    }

    public double getOptimizedDistanceKm() {
        return optimizedDistanceKm;
    }

    public void setOptimizedDistanceKm(double optimizedDistanceKm) {
        this.optimizedDistanceKm = optimizedDistanceKm;
    }

    public double getDistanceReductionPercent() {
        return distanceReductionPercent;
    }

    public void setDistanceReductionPercent(double distanceReductionPercent) {
        this.distanceReductionPercent = distanceReductionPercent;
    }

    public double getBaselineTimeMin() {
        return baselineTimeMin;
    }

    public void setBaselineTimeMin(double baselineTimeMin) {
        this.baselineTimeMin = baselineTimeMin;
    }

    public double getOptimizedTimeMin() {
        return optimizedTimeMin;
    }

    public void setOptimizedTimeMin(double optimizedTimeMin) {
        this.optimizedTimeMin = optimizedTimeMin;
    }

    public double getTimeReductionPercent() {
        return timeReductionPercent;
    }

    public void setTimeReductionPercent(double timeReductionPercent) {
        this.timeReductionPercent = timeReductionPercent;
    }

    public double getBaselineDelayMin() {
        return baselineDelayMin;
    }

    public void setBaselineDelayMin(double baselineDelayMin) {
        this.baselineDelayMin = baselineDelayMin;
    }

    public double getAverageBaselineDelayMin() {
        return averageBaselineDelayMin;
    }

    public void setAverageBaselineDelayMin(double averageBaselineDelayMin) {
        this.averageBaselineDelayMin = averageBaselineDelayMin;
    }

    public int getBaselineOnTimeOrders() {
        return baselineOnTimeOrders;
    }

    public void setBaselineOnTimeOrders(int baselineOnTimeOrders) {
        this.baselineOnTimeOrders = baselineOnTimeOrders;
    }

    public int getBaselineDelayedOrders() {
        return baselineDelayedOrders;
    }

    public void setBaselineDelayedOrders(int baselineDelayedOrders) {
        this.baselineDelayedOrders = baselineDelayedOrders;
    }

    public int getBaselineAtRiskOrders() {
        return baselineAtRiskOrders;
    }

    public void setBaselineAtRiskOrders(int baselineAtRiskOrders) {
        this.baselineAtRiskOrders = baselineAtRiskOrders;
    }

    public double getBaselineOnTimeRatePercent() {
        return baselineOnTimeRatePercent;
    }

    public void setBaselineOnTimeRatePercent(double baselineOnTimeRatePercent) {
        this.baselineOnTimeRatePercent = baselineOnTimeRatePercent;
    }

    public double getOptimizedAllocationRatePercent() {
        return optimizedAllocationRatePercent;
    }

    public void setOptimizedAllocationRatePercent(
            double optimizedAllocationRatePercent) {
        this.optimizedAllocationRatePercent =
                optimizedAllocationRatePercent;
    }

    public int getAvailableVehicles() {
        return availableVehicles;
    }

    public void setAvailableVehicles(int availableVehicles) {
        this.availableVehicles = availableVehicles;
    }

    public int getVehiclesUsed() {
        return vehiclesUsed;
    }

    public void setVehiclesUsed(int vehiclesUsed) {
        this.vehiclesUsed = vehiclesUsed;
    }

    public int getPlanningDays() {
        return planningDays;
    }

    public void setPlanningDays(int planningDays) {
        this.planningDays = planningDays;
    }

    public double getOptimizedCostInr() {
        return optimizedCostInr;
    }

    public void setOptimizedCostInr(double optimizedCostInr) {
        this.optimizedCostInr = optimizedCostInr;
    }
}