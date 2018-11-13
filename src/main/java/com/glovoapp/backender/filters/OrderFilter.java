package com.glovoapp.backender.filters;

import com.glovoapp.backender.entities.Courier;
import com.glovoapp.backender.entities.Order;
import com.glovoapp.backender.entities.Vehicle;
import com.glovoapp.backender.exceptions.InvalidDistanceThresholdException;
import com.glovoapp.backender.utils.DistanceCalculator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class OrderFilter {

    private final String excludedWords;
    private final String excludedWordsDelimiter;
    private final double distanceThresholdInKm;

    public OrderFilter(@Value("${backender.filter.excluded-words}") String excludedWords,
                       @Value("${backender.filter.excluded-words-delimiter}") String excludedWordsDelimiter,
                       @Value("${backender.filter.distance-threshold-in-km}") double distanceThresholdInKm) {
        this.excludedWords = excludedWords;
        this.excludedWordsDelimiter = excludedWordsDelimiter;
        this.distanceThresholdInKm = distanceThresholdInKm;
    }

    /**
     * If the description of the order contains any excluded word, returns true only if the courier is equipped with a Glovo Box.
     */
    public Predicate<Order> filterByCourierBox(final Courier courier) {
        return o -> !containsExcludedWords(o.getDescription()) || courier.getBox();
    }

    /**
     * If the order is further than distanceThresholdInKm to the courier, will only return true if the courier has motorcycle or electric scooter.
     */
    public Predicate<Order> filterByDistanceToCourier(final Courier courier) {
        if (distanceThresholdInKm <= 0) {
            throw new InvalidDistanceThresholdException();
        }

        return o -> {
            final double courierOrderDistance = DistanceCalculator.calculateDistance(o.getDelivery(), courier.getLocation());
            return !(courierOrderDistance > distanceThresholdInKm) ||
                    courier.getVehicle() == Vehicle.MOTORCYCLE || courier.getVehicle() == Vehicle.ELECTRIC_SCOOTER;
        };
    }

    private boolean containsExcludedWords(final String orderDescription) {
        boolean containsExcludedWords = false;
        final String orderDescriptionLower = orderDescription.toLowerCase();

        for (String word : excludedWords.split(excludedWordsDelimiter)) {
            if (orderDescriptionLower.contains(word)) {
                containsExcludedWords = true;
                break;
            }
        }

        return containsExcludedWords;
    }

}
