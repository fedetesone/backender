package com.glovoapp.backender.sorting;

import com.glovoapp.backender.entities.Order;

public class CourierPrioritizedOrder {

    private int priority;
    private Order order;
    private double distanceToCourier;

    public Order getOrder() {
        return order;
    }

    public int getPriority() {
        return priority;
    }

    double getDistanceToCourier() {
        return distanceToCourier;
    }

    CourierPrioritizedOrder withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    CourierPrioritizedOrder withOrder(Order order) {
        this.order = order;
        return this;
    }

    CourierPrioritizedOrder withDistanceToCourier(double distanceToCourier) {
        this.distanceToCourier = distanceToCourier;
        return this;
    }
}
