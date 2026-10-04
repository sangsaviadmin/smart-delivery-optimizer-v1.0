package com.logismart.model;

public class OptimizationSummary {

    // Optimized results
    private int planningDays;
    private int vehiclesUsed;
    private int ordersAllocated;

    private double totalDistanceKm;
    private double totalWeightKg;
    private double totalTimeMin;
    private double totalCostInr;

    private double averageOrdersPerDay;
    private double averageDistancePerDayKm;
    private double averageTimePerDayMin;
    private double averageCostPerDayInr;

    private double averageVehicleUtilizationPercent;
    private double averagePlannedTimePerOrderMin;

    // Baseline planning model
    private int baselinePlanningDays;
    private int baselineVehiclesUsed;
    private int baselineTrips;
    private int baselineOrders;

    private double baselineOrdersPerDay;
    private double baselineDistanceKm;
    private double baselineTimeMin;
    private double baselineDelayMin;
    private double baselineAverageDelayMin;
    private double baselineOnTimeRatePercent;

    private double baselineTotalCostInr;
    private double baselineAverageVehicleUtilizationPercent;

    public int getPlanningDays() {
        return planningDays;
    }

    public void setPlanningDays(int planningDays) {
        this.planningDays = planningDays;
    }

    public int getVehiclesUsed() {
        return vehiclesUsed;
    }

    public void setVehiclesUsed(int vehiclesUsed) {
        this.vehiclesUsed = vehiclesUsed;
    }

    public int getOrdersAllocated() {
        return ordersAllocated;
    }

    public void setOrdersAllocated(int ordersAllocated) {
        this.ordersAllocated = ordersAllocated;
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

    public double getAverageOrdersPerDay() {
        return averageOrdersPerDay;
    }

    public void setAverageOrdersPerDay(double averageOrdersPerDay) {
        this.averageOrdersPerDay = averageOrdersPerDay;
    }

    public double getAverageDistancePerDayKm() {
        return averageDistancePerDayKm;
    }

    public void setAverageDistancePerDayKm(
            double averageDistancePerDayKm) {
        this.averageDistancePerDayKm =
                averageDistancePerDayKm;
    }

    public double getAverageTimePerDayMin() {
        return averageTimePerDayMin;
    }

    public void setAverageTimePerDayMin(
            double averageTimePerDayMin) {
        this.averageTimePerDayMin =
                averageTimePerDayMin;
    }

    public double getAverageCostPerDayInr() {
        return averageCostPerDayInr;
    }

    public void setAverageCostPerDayInr(
            double averageCostPerDayInr) {
        this.averageCostPerDayInr =
                averageCostPerDayInr;
    }

    public double getAverageVehicleUtilizationPercent() {
        return averageVehicleUtilizationPercent;
    }

    public void setAverageVehicleUtilizationPercent(
            double averageVehicleUtilizationPercent) {
        this.averageVehicleUtilizationPercent =
                averageVehicleUtilizationPercent;
    }

    public double getAveragePlannedTimePerOrderMin() {
        return averagePlannedTimePerOrderMin;
    }

    public void setAveragePlannedTimePerOrderMin(
            double averagePlannedTimePerOrderMin) {
        this.averagePlannedTimePerOrderMin =
                averagePlannedTimePerOrderMin;
    }

    public int getBaselinePlanningDays() {
        return baselinePlanningDays;
    }

    public void setBaselinePlanningDays(
            int baselinePlanningDays) {
        this.baselinePlanningDays =
                baselinePlanningDays;
    }

    public int getBaselineVehiclesUsed() {
        return baselineVehiclesUsed;
    }

    public void setBaselineVehiclesUsed(
            int baselineVehiclesUsed) {
        this.baselineVehiclesUsed =
                baselineVehiclesUsed;
    }

    public int getBaselineTrips() {
        return baselineTrips;
    }

    public void setBaselineTrips(int baselineTrips) {
        this.baselineTrips = baselineTrips;
    }

    public int getBaselineOrders() {
        return baselineOrders;
    }

    public void setBaselineOrders(int baselineOrders) {
        this.baselineOrders = baselineOrders;
    }

    public double getBaselineOrdersPerDay() {
        return baselineOrdersPerDay;
    }

    public void setBaselineOrdersPerDay(
            double baselineOrdersPerDay) {
        this.baselineOrdersPerDay =
                baselineOrdersPerDay;
    }

    public double getBaselineDistanceKm() {
        return baselineDistanceKm;
    }

    public void setBaselineDistanceKm(
            double baselineDistanceKm) {
        this.baselineDistanceKm =
                baselineDistanceKm;
    }

    public double getBaselineTimeMin() {
        return baselineTimeMin;
    }

    public void setBaselineTimeMin(
            double baselineTimeMin) {
        this.baselineTimeMin =
                baselineTimeMin;
    }

    public double getBaselineDelayMin() {
        return baselineDelayMin;
    }

    public void setBaselineDelayMin(
            double baselineDelayMin) {
        this.baselineDelayMin =
                baselineDelayMin;
    }

    public double getBaselineAverageDelayMin() {
        return baselineAverageDelayMin;
    }

    public void setBaselineAverageDelayMin(
            double baselineAverageDelayMin) {
        this.baselineAverageDelayMin =
                baselineAverageDelayMin;
    }

    public double getBaselineOnTimeRatePercent() {
        return baselineOnTimeRatePercent;
    }

    public void setBaselineOnTimeRatePercent(
            double baselineOnTimeRatePercent) {
        this.baselineOnTimeRatePercent =
                baselineOnTimeRatePercent;
    }

    public double getBaselineTotalCostInr() {
        return baselineTotalCostInr;
    }

    public void setBaselineTotalCostInr(
            double baselineTotalCostInr) {
        this.baselineTotalCostInr =
                baselineTotalCostInr;
    }

    public double getBaselineAverageVehicleUtilizationPercent() {
        return baselineAverageVehicleUtilizationPercent;
    }

    public void setBaselineAverageVehicleUtilizationPercent(
            double baselineAverageVehicleUtilizationPercent) {
        this.baselineAverageVehicleUtilizationPercent =
                baselineAverageVehicleUtilizationPercent;
    }
}