package com.glovoapp.backender.resources;

import com.glovoapp.backender.entities.Courier;
import com.glovoapp.backender.entities.Location;
import com.glovoapp.backender.entities.Order;
import com.glovoapp.backender.entities.OrderVM;
import com.glovoapp.backender.entities.Vehicle;
import com.glovoapp.backender.exceptions.CourierNotFoundException;
import com.glovoapp.backender.filters.OrderFilter;
import com.glovoapp.backender.repositories.CourierRepository;
import com.glovoapp.backender.repositories.OrderRepository;
import com.glovoapp.backender.resources.OrderResource;
import com.glovoapp.backender.sorting.OrderSorter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderResourceTest {

    private OrderResource subject;
    private List<Order> orders;

    private static final String ORDER_ID_1 = "1";
    private static final String ORDER_ID_2 = "2";
    private static final String ORDER_DESC_1 = "This is the description 1";
    private static final String ORDER_DESC_2 = "This is the description 2";

    private Order order1;
    private Order order2;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CourierRepository courierRepository;
    @Mock
    private OrderFilter orderFilter;
    @Mock
    private OrderSorter orderSorter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        subject = new OrderResource(orderRepository, courierRepository, orderSorter, orderFilter);

        // This order has the excluded word "pizza"
        order1 = new Order().withId(ORDER_ID_1)
                .withDescription(ORDER_DESC_1)
                .withFood(true)
                .withVip(false)
                .withPickup(new Location(41.3965463, 2.1963997))
                .withDelivery(new Location(41.407834, 2.1675979));

        order2 = new Order().withId(ORDER_ID_2)
                .withDescription(ORDER_DESC_2)
                .withFood(true)
                .withVip(false)
                .withPickup(new Location(40.4167047, -3.7035825))
                .withDelivery(new Location(40.4167047, -3.7035825));

        orders = Arrays.asList(order1, order2);
    }

    @Test
    public void orders_sucess() {
        when(orderRepository.findAll()).thenReturn(orders);
        List<OrderVM> orderVMList = subject.orders();

        assertNotNull(orderVMList);
        assertFalse(orderVMList.isEmpty());
        assertEquals(2, orderVMList.size());
        assertEquals(ORDER_ID_1, orderVMList.get(0).getId());
        assertEquals(ORDER_DESC_1, orderVMList.get(0).getDescription());
        assertEquals(ORDER_ID_2, orderVMList.get(1).getId());
        assertEquals(ORDER_DESC_2, orderVMList.get(1).getDescription());

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    public void orders_emptyList_sucess() {
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());
        List<OrderVM> orderVMList = subject.orders();

        assertNotNull(orderVMList);
        assertTrue(orderVMList.isEmpty());

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    public void getCourierOrders_courierNotFound() {
        final String courierId = "courier-not-found";
        when(courierRepository.findById(courierId)).thenReturn(null);

        CourierNotFoundException e = assertThrows(CourierNotFoundException.class, () -> subject.getCourierOrders(courierId));
        assertEquals("Courier not found.", e.getMessage());
    }

    @Test
    public void getCourierOrders_success() {
        final String courierId = "courier-1";

        final Courier courier = new Courier().withId(courierId)
                .withBox(true)
                .withName("Manolo Escobar")
                .withVehicle(Vehicle.BICYCLE)
                .withLocation(new Location(41.3965463, 2.1963997));

        when(courierRepository.findById(courierId)).thenReturn(courier);
        when(orderRepository.findAll()).thenReturn(orders);
        when(orderFilter.filterByDistanceToCourier(courier)).thenReturn(o -> true);
        when(orderFilter.filterByCourierBox(courier)).thenReturn(o -> true);
        when(orderSorter.sortOrders(any(), eq(courier))).thenReturn(orders.stream());

        final List<OrderVM> orderVMList = subject.getCourierOrders(courierId);

        assertNotNull(orderVMList);
        assertFalse(orderVMList.isEmpty());
        assertEquals(2, orderVMList.size());
        assertEquals(ORDER_ID_1, orderVMList.get(0).getId());
        assertEquals(ORDER_DESC_1, orderVMList.get(0).getDescription());
        assertEquals(ORDER_ID_2, orderVMList.get(1).getId());
        assertEquals(ORDER_DESC_2, orderVMList.get(1).getDescription());

        verify(courierRepository, times(1)).findById(courierId);
        verify(orderRepository, times(1)).findAll();
        verify(orderFilter, times(1)).filterByDistanceToCourier(courier);
        verify(orderFilter, times(1)).filterByCourierBox(courier);
        verify(orderSorter, times(1)).sortOrders(any(), eq(courier));
    }

    @Test
    public void getCourierOrders_ordersFilteredOut() {
        final String courierId = "courier-1";

        final Courier courier = new Courier().withId(courierId)
                .withBox(true)
                .withName("Manolo Escobar")
                .withVehicle(Vehicle.BICYCLE)
                .withLocation(new Location(41.3965463, 2.1963997));

        when(courierRepository.findById(courierId)).thenReturn(courier);
        when(orderRepository.findAll()).thenReturn(orders);
        when(orderFilter.filterByCourierBox(courier)).thenReturn(o -> false);
        when(orderFilter.filterByDistanceToCourier(courier)).thenReturn(o -> true);

        final List<OrderVM> orderVMList = subject.getCourierOrders(courierId);

        ArgumentCaptor<Stream<Order>> filteredOrdersCaptor = ArgumentCaptor.forClass(Stream.class);
        verify(orderSorter).sortOrders(filteredOrdersCaptor.capture(), eq(courier));
        Stream<Order> filteredOrders = filteredOrdersCaptor.getValue();

        assertNotNull(filteredOrders);
        assertTrue(filteredOrders.collect(Collectors.toList()).isEmpty());

        assertNotNull(orderVMList);
        assertTrue(orderVMList.isEmpty());

        verify(courierRepository, times(1)).findById(courierId);
        verify(orderRepository, times(1)).findAll();
        verify(orderFilter, times(1)).filterByDistanceToCourier(courier);
        verify(orderFilter, times(1)).filterByCourierBox(courier);
        verify(orderSorter, times(1)).sortOrders(any(), eq(courier));
    }

    @Test
    public void getCourierOrders_ordersSorted() {
        final String courierId = "courier-1";

        final Courier courier = new Courier().withId(courierId)
                .withBox(true)
                .withName("Manolo Escobar")
                .withVehicle(Vehicle.BICYCLE)
                .withLocation(new Location(41.3965463, 2.1963997));

        when(courierRepository.findById(courierId)).thenReturn(courier);
        when(orderRepository.findAll()).thenReturn(orders);
        when(orderFilter.filterByDistanceToCourier(courier)).thenReturn(o -> true);
        when(orderFilter.filterByCourierBox(courier)).thenReturn(o -> true);

        Stream<Order> ordersSorted = Stream.of(order2, order1);
        when(orderSorter.sortOrders(any(), eq(courier))).thenReturn(ordersSorted);

        final List<OrderVM> orderVMList = subject.getCourierOrders(courierId);

        assertNotNull(orderVMList);
        assertFalse(orderVMList.isEmpty());
        assertEquals(2, orderVMList.size());
        assertEquals(ORDER_ID_2, orderVMList.get(0).getId()); // First order2.
        assertEquals(ORDER_DESC_2, orderVMList.get(0).getDescription());
        assertEquals(ORDER_ID_1, orderVMList.get(1).getId());
        assertEquals(ORDER_DESC_1, orderVMList.get(1).getDescription());

        verify(courierRepository, times(1)).findById(courierId);
        verify(orderRepository, times(1)).findAll();
        verify(orderFilter, times(1)).filterByDistanceToCourier(courier);
        verify(orderFilter, times(1)).filterByCourierBox(courier);
        verify(orderSorter, times(1)).sortOrders(any(), eq(courier));
    }


}
