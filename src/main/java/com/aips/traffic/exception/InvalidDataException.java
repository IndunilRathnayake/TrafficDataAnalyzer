/*
 * Copyright (c) 2022, Indunil Rathnayake. All Rights Reserved.
 */

package com.aips.traffic.exception;

/**
 * Handling exception occurs while validating the input data
 */
public class InvalidDataException extends RuntimeException {

    private static final long serialVersionUID = 7718829535143693558L;

    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
