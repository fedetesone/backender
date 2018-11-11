package com.glovoapp.backender;

import com.glovoapp.backender.entities.Courier;
import com.glovoapp.backender.entities.Location;
import com.glovoapp.backender.entities.Order;
import com.glovoapp.backender.entities.Vehicle;
import com.glovoapp.backender.exceptions.InvalidDistanceThresholdException;
import com.glovoapp.backender.filters.OrderFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderFilterTest {

    private OrderFilter subject;

    private Order order1;
    private Order order2;
    private Stream<Order> orderStream;

    @BeforeEach
    public void setUp() {
        final String excludedWords = "pizza,cake";
        final String excludedWordsDelimiter = ",";
        final double distanceThresholdInKm = 5;

        subject = new OrderFilter(excludedWords, excludedWordsDelimiter, distanceThresholdInKm);

        // This order has the excluded word "pizza"
        order1 = new Order().withId("order-1")
                .withDescription("I want a pizza cut into very small slices")
                .withFood(true)
                .withVip(false)
                .withPickup(new Location(41.3965463, 2.1963997))
                .withDelivery(new Location(41.407834, 2.1675979)); // Barcelona

        order2 = new Order().withId("order-1")
                .withDescription("I want a steak very big")
                .withFood(true)
                .withVip(false)
                .withPickup(new Location(40.4167047, -3.7035825))
                .withDelivery(new Location(40.4167047, -3.7035825)); // Madrid

        orderStream = Stream.of(order1, order2);
    }

    @Test
    public void filterByCourierBox_courierWithBox_returnAllOrders() {
        final Courier courierWithBox = new Courier().withId("courier-1")
                .withBox(true)
                .withName("Manolo Escobar")
                .withVehicle(Vehicle.MOTORCYCLE)
                .withLocation(new Location(41.3965463, 2.1963997));

        final List<Order> filteredOrders = orderStream
                .filter(subject.filterByCourierBox(courierWithBox))
                .collect(Collectors.toList());

        assertNotNull(filteredOrders);
        assertEquals(2, filteredOrders.size());
        assertEquals(order1, filteredOrders.get(0));
        assertEquals(order2, filteredOrders.get(1));
    }

    @Test
    public void filterByCourierBox_courierWithoutBox_returnNotExcludedOrders() {
        final Courier courierWithoutBox = new Courier().withId("courier-2")
                .withBox(false)
                .withName("Manolo Escobar does not have box")
                .withVehicle(Vehicle.BICYCLE)
                .withLocation(new Location(41.3965463, 2.1963997));

        final List<Order> filteredOrders = orderStream
                .filter(subject.filterByCourierBox(courierWithoutBox))
                .collect(Collectors.toList());

        assertNotNull(filteredOrders);
        assertEquals(1, filteredOrders.size());
        assertEquals(order2, filteredOrders.get(0));
    }

    @Test
    public void filterByCourierBox_emptyOrderList_returnEmptyOrderList() {
        final Courier courierWithoutBox = new Courier().withId("courier-2")
                .withBox(false)
                .withName("Manolo Escobar does not have box")
                .withVehicle(Vehicle.BICYCLE)
                .withLocation(new Location(41.3965463, 2.1963997));

        final List<Order> filteredOrders = Stream.<Order>empty()
                .filter(subject.filterByCourierBox(courierWithoutBox))
                .collect(Collectors.toList());

        assertNotNull(filteredOrders);
        assertTrue(filteredOrders.isEmpty());
    }

    @Test
    public void filterByDistanceToCourier_oneOrderCloserThanThreshold_courierWithBicicle() {
        final Courier courierClose = new Courier().withId("courier-1")
                .withBox(true)
                .withName("Manolo Escobar")
                .withVehicle(Vehicle.BICYCLE)
                .withLocation(new Location(41.3965463, 2.1963997)); // Barcelona

        final List<Order> filteredOrders = orderStream
                .filter(subject.filterByDistanceToCourier(courierClose))
                .collect(Collectors.toList());

        assertNotNull(filteredOrders);
        assertEquals(1, filteredOrders.size());
        assertEquals(order1, filteredOrders.get(0));
    }

    @Test
    public void filterByDistanceToCourier_oneOrderCloserThanThreshold_courierWithScooter() {
        final Courier courierClose = new Courier().withId("courier-1")
                .withBox(true)
                .withName("Manolo Escobar")
                .withVehicle(Vehicle.ELECTRIC_SCOOTER)
                .withLocation(new Location(41.3965463, 2.1963997)); // Barcelona

        final List<Order> filteredOrders = orderStream
                .filter(subject.filterByDistanceToCourier(courierClose))
                .collect(Collectors.toList());

        assertNotNull(filteredOrders);
        assertEquals(2, filteredOrders.size());
        assertEquals(order1, filteredOrders.get(0));
        assertEquals(order2, filteredOrders.get(1)); // From Barcelona to Madrid by Scooter :)
    }

    @Test
    public void filterByDistanceToCourier_ordersTooFar_courierWithBicicle() {
        final double distanceThresholdInKm = 1;

        subject = new OrderFilter(null, null, distanceThresholdInKm);

        final Courier courierClose = new Courier().withId("courier-1")
                .withBox(true)
                .withName("Manolo Escobar")
                .withVehicle(Vehicle.BICYCLE)
                .withLocation(new Location(41.3965463, 2.1963997)); // Barcelona

        final List<Order> filteredOrders = orderStream
                .filter(subject.filterByDistanceToCourier(courierClose))
                .collect(Collectors.toList());

        assertNotNull(filteredOrders);
        assertTrue(filteredOrders.isEmpty());
    }

    @Test
    public void applyBothFilters_withoutBox_inMotorcicle_getsOrder2() {
        final Courier courier = new Courier().withId("courier-1")
                .withBox(false)
                .withName("Manolo Escobar does not have box")
                .withVehicle(Vehicle.MOTORCYCLE)
                .withLocation(new Location(41.3965463, 2.1963997));

        final List<Order> filteredOrders = orderStream
                .filter(subject.filterByCourierBox(courier))
                .filter(subject.filterByDistanceToCourier(courier))
                .collect(Collectors.toList());

        assertNotNull(filteredOrders);
        assertEquals(1, filteredOrders.size());
        assertEquals(order2, filteredOrders.get(0));
    }

    @Test
    public void filterByDistanceToCourier_invalidThreshold_exceptionThrown() {
        final double distanceThresholdInKm = 0;

        subject = new OrderFilter(null, null, distanceThresholdInKm);

        InvalidDistanceThresholdException e = assertThrows(InvalidDistanceThresholdException.class,
                () -> subject.filterByDistanceToCourier(new Courier()));
        assertEquals("Distance threshold has an invalid value.", e.getMessage());
    }

}
