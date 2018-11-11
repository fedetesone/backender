package com.glovoapp.backender.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Courier not found.")
public class CourierNotFoundException extends RuntimeException {
    public CourierNotFoundException() {
        super("Courier not found.");
    }
}