package com.glovoapp.backender.sorting;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "backender.sorting")
public class SortingPriorityConfig {

    private final Map<Integer, String> sortingCriteria = new HashMap<>();

    public Map<Integer, String> getSortingCriteria() {
        return sortingCriteria;
    }
}
