package com.glovoapp.backender.exceptions;

public class InvalidDistanceThresholdException extends RuntimeException {

    public InvalidDistanceThresholdException() {
        super("Distance threshold has an invalid value.");
    }

}
