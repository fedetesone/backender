package com.glovoapp.backender;

import com.glovoapp.backender.entities.Courier;
import com.glovoapp.backender.entities.Location;
import com.glovoapp.backender.entities.Order;
import com.glovoapp.backender.entities.Vehicle;
import com.glovoapp.backender.sorting.OrderSorter;
import com.glovoapp.backender.sorting.SortingCriteria;
import com.glovoapp.backender.sorting.SortingPriorityConfig;
import com.glovoapp.backender.utils.DistanceCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class OrderSorterTest {

    private OrderSorter subject;
    private double slotDistanceInKm = 0.5;
    private Courier courier;
    private Stream<Order> orderStream;

    private Order order1;
    private Order order2;
    private Order order3;
    private Order order4;
    private Order order5;
    private Order order6;
    private Order order7;
    private Order order8;
    private Order order9;
    private Order order10;
    private Order order11;

    @Mock
    private SortingPriorityConfig sortingPriorityConfig;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        courier = new Courier().withId("courier-1")
                .withBox(true)
                .withName("Manolo Escobar")
                .withVehicle(Vehicle.MOTORCYCLE)
                .withLocation(new Location(41.3965463, 2.1963997)); // Courier in Barcelona.

        // 0-500 km -> Priority 0.
        order1 = createOrder("1", true, true, new Location(41.397117, 2.196976), 0.079);
        order2 = createOrder("2", true, false, new Location(41.3972496, 2.1960500), 0.083);
        order3 = createOrder("3", false, true, new Location(41.3973345, 2.19493746), 0.150);
        order4 = createOrder("4", false, false, new Location(41.3966585, 2.19393968), 0.205);
        order5 = createOrder("5", false, false, new Location(41.3950086, 2.1942186), 0.249);

        // 500-1000 km -> Priority 1.
        order6 = createOrder("6", false, true, new Location(41.4014394, 2.2030515), 0.777);
        order7 = createOrder("7", true, false, new Location(41.402799, 2.202000), 0.837);

        // 1000-1500 km -> Priority 2.
        order8 = createOrder("8", false, false, new Location(41.4034674, 2.2066564), 1.15);
        order9 = createOrder("9", true, true, new Location(41.405326, 2.206678), 1.299);

        // > 1500 km -> Lower priority.
        order10 = createOrder("10", true, true, new Location(41.4154934, 2.2122804), 2.488);
        order11 = createOrder("11", true, true, new Location(41.419637, 2.201211), 2.598);

        final List<Order> orders = Arrays.asList(order1, order2, order3, order4, order5, order6, order7, order8, order9, order10, order11);

        // Shuffle!
        Collections.shuffle(orders);

        orderStream = orders.stream(); // Converting to stream because it is usable in a more direct way.

        // This is the default priority order.
        final Map<Integer, String> sortingCriteriaMap = new HashMap<>();
        sortingCriteriaMap.put(1, SortingCriteria.DISTANCE_SLOT.toString());
        sortingCriteriaMap.put(2, SortingCriteria.VIP.toString());
        sortingCriteriaMap.put(3, SortingCriteria.FOOD.toString());
        sortingCriteriaMap.put(4, SortingCriteria.ABSOLUTE_DISTANCE.toString());

        when(sortingPriorityConfig.getSortingCriteria()).thenReturn(sortingCriteriaMap);

        subject = new OrderSorter(sortingPriorityConfig, slotDistanceInKm);
    }

    @Test
    void createPrioritizedOrder_testPriorities() {
        assertEquals(0, subject.createPrioritizedOrder(order1, courier).getPriority());
        assertEquals(0, subject.createPrioritizedOrder(order2, courier).getPriority());
        assertEquals(0, subject.createPrioritizedOrder(order3, courier).getPriority());
        assertEquals(0, subject.createPrioritizedOrder(order4, courier).getPriority());
        assertEquals(0, subject.createPrioritizedOrder(order5, courier).getPriority());
        assertEquals(1, subject.createPrioritizedOrder(order6, courier).getPriority());
        assertEquals(1, subject.createPrioritizedOrder(order7, courier).getPriority());
        assertEquals(2, subject.createPrioritizedOrder(order8, courier).getPriority());
        assertEquals(2, subject.createPrioritizedOrder(order9, courier).getPriority());
        assertEquals(4, subject.createPrioritizedOrder(order10, courier).getPriority());
        assertEquals(5, subject.createPrioritizedOrder(order11, courier).getPriority());
    }

    @Test
    public void sortOrders_defaultOrder() {
        final Stream<Order> sortedStream = subject.sortOrders(orderStream, courier);

        List<Order> sortedOrders = sortedStream.collect(Collectors.toList());

        assertNotNull(sortedOrders);
        assertFalse(sortedOrders.isEmpty());
        assertEquals(11, sortedOrders.size());
        assertEquals(order1, sortedOrders.get(0));
        assertEquals(order2, sortedOrders.get(1));
        assertEquals(order3, sortedOrders.get(2));
        assertEquals(order4, sortedOrders.get(3));
        assertEquals(order5, sortedOrders.get(4));
        assertEquals(order7, sortedOrders.get(5));
        assertEquals(order6, sortedOrders.get(6));
        assertEquals(order9, sortedOrders.get(7));
        assertEquals(order8, sortedOrders.get(8));
        assertEquals(order10, sortedOrders.get(9));
        assertEquals(order11, sortedOrders.get(10));
    }

    @Test
    public void sortOrders_Vip_ThenFood_ThenSlots_ThenDistance() {
        final Map<Integer, String> sortingCriteriaMap = new HashMap<>();
        sortingCriteriaMap.put(1, SortingCriteria.VIP.toString());
        sortingCriteriaMap.put(2, SortingCriteria.FOOD.toString());
        sortingCriteriaMap.put(3, SortingCriteria.DISTANCE_SLOT.toString());
        sortingCriteriaMap.put(4, SortingCriteria.ABSOLUTE_DISTANCE.toString());

        when(sortingPriorityConfig.getSortingCriteria()).thenReturn(sortingCriteriaMap);

        subject = new OrderSorter(sortingPriorityConfig, slotDistanceInKm);

        final Stream<Order> sortedStream = subject.sortOrders(orderStream, courier);

        List<Order> sortedOrders = sortedStream.collect(Collectors.toList());

        assertNotNull(sortedOrders);
        assertFalse(sortedOrders.isEmpty());
        assertEquals(11, sortedOrders.size());
        assertEquals(order1, sortedOrders.get(0));
        assertEquals(order9, sortedOrders.get(1));
        assertEquals(order10, sortedOrders.get(2));
        assertEquals(order11, sortedOrders.get(3));
        assertEquals(order2, sortedOrders.get(4));
        assertEquals(order7, sortedOrders.get(5));
        assertEquals(order3, sortedOrders.get(6));
        assertEquals(order6, sortedOrders.get(7));
        assertEquals(order4, sortedOrders.get(8));
        assertEquals(order5, sortedOrders.get(9));
        assertEquals(order8, sortedOrders.get(10));
    }

    @Test
    public void sortOrders_Food_ThenDistance_ThenSlots_ThenVip() {
        final Map<Integer, String> sortingCriteriaMap = new HashMap<>();
        sortingCriteriaMap.put(1, SortingCriteria.FOOD.toString());
        sortingCriteriaMap.put(2, SortingCriteria.ABSOLUTE_DISTANCE.toString());
        sortingCriteriaMap.put(3, SortingCriteria.DISTANCE_SLOT.toString());
        sortingCriteriaMap.put(4, SortingCriteria.VIP.toString());

        when(sortingPriorityConfig.getSortingCriteria()).thenReturn(sortingCriteriaMap);

        subject = new OrderSorter(sortingPriorityConfig, slotDistanceInKm);

        final Stream<Order> sortedStream = subject.sortOrders(orderStream, courier);

        List<Order> sortedOrders = sortedStream.collect(Collectors.toList());

        assertNotNull(sortedOrders);
        assertFalse(sortedOrders.isEmpty());
        assertEquals(11, sortedOrders.size());
        assertEquals(order1, sortedOrders.get(0));
        assertEquals(order3, sortedOrders.get(1));
        assertEquals(order6, sortedOrders.get(2));
        assertEquals(order9, sortedOrders.get(3));
        assertEquals(order10, sortedOrders.get(4));
        assertEquals(order11, sortedOrders.get(5));
        assertEquals(order2, sortedOrders.get(6));
        assertEquals(order4, sortedOrders.get(7));
        assertEquals(order5, sortedOrders.get(8));
        assertEquals(order7, sortedOrders.get(9));
        assertEquals(order8, sortedOrders.get(10));
    }

    @Test
    public void sortOrders_Distance_ThenSlots_ThenFood_ThenVip() {
        final Map<Integer, String> sortingCriteriaMap = new HashMap<>();
        sortingCriteriaMap.put(1, SortingCriteria.ABSOLUTE_DISTANCE.toString());
        sortingCriteriaMap.put(2, SortingCriteria.DISTANCE_SLOT.toString());
        sortingCriteriaMap.put(3, SortingCriteria.FOOD.toString());
        sortingCriteriaMap.put(4, SortingCriteria.VIP.toString());

        when(sortingPriorityConfig.getSortingCriteria()).thenReturn(sortingCriteriaMap);

        subject = new OrderSorter(sortingPriorityConfig, slotDistanceInKm);

        final Stream<Order> sortedStream = subject.sortOrders(orderStream, courier);

        List<Order> sortedOrders = sortedStream.collect(Collectors.toList());

        assertNotNull(sortedOrders);
        assertFalse(sortedOrders.isEmpty());
        assertEquals(11, sortedOrders.size());
        assertEquals(order1, sortedOrders.get(0));
        assertEquals(order2, sortedOrders.get(1));
        assertEquals(order3, sortedOrders.get(2));
        assertEquals(order4, sortedOrders.get(3));
        assertEquals(order5, sortedOrders.get(4));
        assertEquals(order6, sortedOrders.get(5));
        assertEquals(order7, sortedOrders.get(6));
        assertEquals(order8, sortedOrders.get(7));
        assertEquals(order9, sortedOrders.get(8));
        assertEquals(order10, sortedOrders.get(9));
        assertEquals(order11, sortedOrders.get(10));
    }

    @Test
    public void sortOrders_emptyStream() {
        final Stream<Order> sortedStream = subject.sortOrders(Stream.empty(), courier);

        List<Order> sortedOrders = sortedStream.collect(Collectors.toList());

        assertNotNull(sortedOrders);
        assertTrue(sortedOrders.isEmpty());
    }

    private Order createOrder(String id, boolean isVip, boolean isFood, Location deliveryLocation, double distanceToCourier) {
        // Assertions at this point make easier not to make a mistake in locations.
        assertEquals(distanceToCourier, DistanceCalculator.calculateDistance(courier.getLocation(), deliveryLocation), 0.001);

        return new Order().withId(id)
                .withDescription("any description")
                .withFood(isFood)
                .withVip(isVip)
                .withDelivery(deliveryLocation);
    }

}
