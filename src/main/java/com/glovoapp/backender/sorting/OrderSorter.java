package com.glovoapp.backender.sorting;

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
        // The comparators work over CourierPrioritizedOrder. Then the order is extracted again.
        return orders
                .map(o -> new CourierPrioritizedOrder(o, courier, slotDistanceInKm))
                .sorted(compareOrders())
                .map(CourierPrioritizedOrder::getOrder);
    }

    private Comparator<CourierPrioritizedOrder> compareOrders() {
        return sortingCriteriaMap.get(SortingCriteria.valueOf(sortingCriteria.get(1)))
                .thenComparing(sortingCriteriaMap.get(SortingCriteria.valueOf(sortingCriteria.get(2))))
                .thenComparing(sortingCriteriaMap.get(SortingCriteria.valueOf(sortingCriteria.get(3))))
                .thenComparing(sortingCriteriaMap.get(SortingCriteria.valueOf(sortingCriteria.get(4))));
    }

    // Orders that are close to the courier, in slots.
    private static Comparator<CourierPrioritizedOrder> sortByPriority() {
        return Comparator.comparing(CourierPrioritizedOrder::getPriority);
    }

    // Orders that belong to a VIP customer.
    private static Comparator<CourierPrioritizedOrder> sortByVip() {
        return Comparator.comparing(po -> !po.getOrder().getVip()); // Reverse order (true values first).
    }

    // Orders that are food.
    private static Comparator<CourierPrioritizedOrder> sortByFood() {
        return Comparator.comparing(po -> !po.getOrder().getFood()); // Reverse order (true values first).
    }

    // By absolute distance.
    private static Comparator<CourierPrioritizedOrder> sortByAbsoluteDistance() {
        return Comparator.comparing(CourierPrioritizedOrder::getDistanceToCourier);
    }
}
