/*
 * Copyright (c) 2022, Indunil Rathnayake. All Rights Reserved.
 */

package com.aips.traffic.exception;

/**
 * Handling exception occurs while processing traffic data
 */
public class TrafficDataProcessException extends RuntimeException {

    private static final long serialVersionUID = 7718829535143693558L;

    public TrafficDataProcessException(String message) {
        super(message);
    }

    public TrafficDataProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
