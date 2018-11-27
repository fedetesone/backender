package com.glovoapp.backender.entities;

public class Stat {
    private Integer numberOfOrders;
    private Integer numberOfCouriers;
    private float percentageOfNonFoodOrders;
    private double averageCourierToPickupDistanceInMeters;

    public Stat(Integer numberOfOrders, Integer numberOfCouriers, float percentageOfFoodOrders, double averageCourierToPickupDistanceInMeters) {
        this.numberOfOrders = numberOfOrders;
        this.numberOfCouriers = numberOfCouriers;
        this.percentageOfNonFoodOrders = percentageOfFoodOrders;
        this.averageCourierToPickupDistanceInMeters = averageCourierToPickupDistanceInMeters;
    }

    public Integer getNumberOfOrders() {
        return numberOfOrders;
    }

    public void setNumberOfOrders(Integer numberOfOrders) {
        this.numberOfOrders = numberOfOrders;
    }

    public Integer getNumberOfCouriers() {
        return numberOfCouriers;
    }

    public void setNumberOfCouriers(Integer numberOfCouriers) {
        this.numberOfCouriers = numberOfCouriers;
    }

    public float getPercentageOfNonFoodOrders() {
        return percentageOfNonFoodOrders;
    }

    public void setPercentageOfNonFoodOrders(float percentageOfNonFoodOrders) {
        this.percentageOfNonFoodOrders = percentageOfNonFoodOrders;
    }

    public double getAverageCourierToPickupDistanceInMeters() {
        return averageCourierToPickupDistanceInMeters;
    }

    public void setAverageCourierToPickupDistanceInMeters(double averageCourierToPickupDistanceInMeters) {
        this.averageCourierToPickupDistanceInMeters = averageCourierToPickupDistanceInMeters;
    }
}
