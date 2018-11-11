package com.glovoapp.backender.sorting;

import com.glovoapp.backender.utils.DistanceCalculator;
import com.glovoapp.backender.entities.Courier;
import com.glovoapp.backender.entities.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class OrderSorter {

    private final Map<Integer, String> sortingCriteria;
    private final double slotDistanceInKm;

    private static final Map<SortingCriteria, Comparator<CourierPrioritizedOrder>> sortingCriteriaMap = createCriteriaMap();

    private static Map<SortingCriteria, Comparator<CourierPrioritizedOrder>> createCriteriaMap() {
        final Map<SortingCriteria, Comparator<CourierPrioritizedOrder>> criteriaMap = new HashMap<>();
        criteriaMap.put(SortingCriteria.DISTANCE_SLOT, sortByPriority());
        criteriaMap.put(SortingCriteria.VIP, sortByVip());
        criteriaMap.put(SortingCriteria.FOOD, sortByFood());
        criteriaMap.put(SortingCriteria.ABSOLUTE_DISTANCE, sortByAbsoluteDistance());

        return criteriaMap;
    }

    @Autowired
    public OrderSorter(SortingPriorityConfig sortingPriorityConfig,
                       @Value("${backender.sorting.slot-distance-in-km}") double slotDistanceInKm) {
        this.sortingCriteria = sortingPriorityConfig.getSortingCriteria();
        this.slotDistanceInKm = slotDistanceInKm;
    }

    public Stream<Order> sortOrders(Stream<Order> orders, Courier courier) {
        return orders
                .map(o -> createPrioritizedOrder(o, courier))
                .sorted(compareOrders())
                .map(CourierPrioritizedOrder::getOrder);
    }

    private Comparator<CourierPrioritizedOrder> compareOrders() {
        return sortingCriteriaMap.get(SortingCriteria.valueOf(sortingCriteria.get(1)))
                .thenComparing(sortingCriteriaMap.get(SortingCriteria.valueOf(sortingCriteria.get(2))))
                .thenComparing(sortingCriteriaMap.get(SortingCriteria.valueOf(sortingCriteria.get(3))))
                .thenComparing(sortingCriteriaMap.get(SortingCriteria.valueOf(sortingCriteria.get(4))));
    }

    private static Comparator<CourierPrioritizedOrder> sortByPriority() {
        return Comparator.comparing(CourierPrioritizedOrder::getPriority);
    }

    private static Comparator<CourierPrioritizedOrder> sortByVip() {
        return Comparator.comparing(po -> !po.getOrder().getVip()); // Reverse order (true values first).
    }

    private static Comparator<CourierPrioritizedOrder> sortByFood() {
        return Comparator.comparing(po -> !po.getOrder().getFood()); // Reverse order (true values first).
    }

    private static Comparator<CourierPrioritizedOrder> sortByAbsoluteDistance() {
        return Comparator.comparing(CourierPrioritizedOrder::getDistanceToCourier);
    }

    public CourierPrioritizedOrder createPrioritizedOrder(Order order, Courier courier) {
        double distanceToCourier = getDistanceToCourier(order, courier);

        return new CourierPrioritizedOrder()
                .withPriority((int) (distanceToCourier / slotDistanceInKm))
                .withOrder(order)
                .withDistanceToCourier(distanceToCourier);
    }

    private double getDistanceToCourier(Order order, Courier courier) {
        return DistanceCalculator.calculateDistance(order.getDelivery(), courier.getLocation());
    }
}
