package com.glovoapp.backender.resources;

import com.glovoapp.backender.entities.Courier;
import com.glovoapp.backender.entities.Order;
import com.glovoapp.backender.entities.OrderVM;
import com.glovoapp.backender.exceptions.CourierNotFoundException;
import com.glovoapp.backender.filters.OrderFilter;
import com.glovoapp.backender.repositories.CourierRepository;
import com.glovoapp.backender.repositories.OrderRepository;
import com.glovoapp.backender.sorting.OrderSorter;
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

}
