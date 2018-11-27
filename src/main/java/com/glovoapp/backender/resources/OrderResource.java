package com.glovoapp.backender.resources;

import com.glovoapp.backender.entities.Courier;
import com.glovoapp.backender.entities.Order;
import com.glovoapp.backender.entities.OrderVM;
import com.glovoapp.backender.entities.Stat;
import com.glovoapp.backender.exceptions.CourierNotFoundException;
import com.glovoapp.backender.filters.OrderFilter;
import com.glovoapp.backender.repositories.CourierRepository;
import com.glovoapp.backender.repositories.OrderRepository;
import com.glovoapp.backender.sorting.OrderSorter;
import com.glovoapp.backender.utils.DistanceCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class OrderResource {

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final OrderSorter orderSorter;
    private final OrderFilter orderFilter;

    @Autowired
    public OrderResource(OrderRepository orderRepository,
                         CourierRepository courierRepository,
                         OrderSorter orderSorter,
                         OrderFilter orderFilter) {
        this.orderRepository = orderRepository;
        this.courierRepository = courierRepository;
        this.orderSorter = orderSorter;
        this.orderFilter = orderFilter;
    }

    @GetMapping(value = "/orders")
    @ResponseBody
    public List<OrderVM> orders() {
        return orderRepository.findAll()
                .stream()
                .map(order -> new OrderVM(order.getId(), order.getDescription()))
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/orders/{courierId}")
    @ResponseBody
    public List<OrderVM> getCourierOrders(@PathVariable("courierId") final String courierId) {
        final Courier courier = courierRepository.findById(courierId);
        if (courier == null) {
            throw new CourierNotFoundException();
        }

        final Stream<Order> filteredOrders = orderRepository.findAll()
                .stream()
                .filter(orderFilter.filterByCourierBox(courier))
                .filter(orderFilter.filterByDistanceToCourier(courier));

        return orderSorter.sortOrders(filteredOrders, courier)
                .map(order -> new OrderVM(order.getId(), order.getDescription()))
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/stats")
    @ResponseBody
    public Stat getStats() {
        final List<Order> allOrders = orderRepository.findAll();
        final List<Courier> couriers = courierRepository.findAll();

        final int numberOfCouriers = couriers.size();
        final long numberOfNonFoodOrders = allOrders.stream()
                .filter(o -> !o.getFood())
                .count();

        final int totalOrdersCount = allOrders.size();
        final float foodPercentage = (float) numberOfNonFoodOrders / (float) totalOrdersCount;

        // Average courier-to-pickup distance in meters
        double averageCourierToPickupDistanceInMeters;
        int countOfDistances = numberOfCouriers * totalOrdersCount;
        double totalDistanceInKilometers = 0;

        for (Courier c : couriers) {
            for (Order o : allOrders) {
                totalDistanceInKilometers += DistanceCalculator.calculateDistance(c.getLocation(), o.getPickup());
            }
        }

        averageCourierToPickupDistanceInMeters = (totalDistanceInKilometers / countOfDistances) * 1000;
        return new Stat(totalOrdersCount, numberOfCouriers, foodPercentage, averageCourierToPickupDistanceInMeters);
    }


}
