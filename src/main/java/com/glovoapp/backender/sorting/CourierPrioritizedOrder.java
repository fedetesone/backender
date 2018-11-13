package com.glovoapp.backender.sorting;

import com.glovoapp.backender.entities.Courier;
import com.glovoapp.backender.entities.Order;
import com.glovoapp.backender.utils.DistanceCalculator;

/**
 * This class was created in order to calculate the distance to the courier once, since it is used
 * by the DISTANCE_SLOT and the ABSOLUTE_DISTANCE criteria.
 */
public class CourierPrioritizedOrder {

    private int priority;
    private Order order;
    private double distanceToCourier;

    CourierPrioritizedOrder(Order order, Courier courier, double slotDistanceInKm) {
        final double distanceToCourier = DistanceCalculator.calculateDistance(order.getDelivery(), courier.getLocation());

        // Each slow will have different priority. Closer -> More priority (lower value).
        this.priority = (int) (distanceToCourier / slotDistanceInKm);
        this.order = order;
        this.distanceToCourier = distanceToCourier;
    }

    // Not allowed to use externally.
    private CourierPrioritizedOrder() {

    }

    public Order getOrder() {
        return order;
    }

    public int getPriority() {
        return priority;
    }

    double getDistanceToCourier() {
        return distanceToCourier;
    }
}
