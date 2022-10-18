/*
 * Copyright (c) 2022, Indunil Rathnayake. All Rights Reserved.
 */

package com.aips.traffic.process;

import com.aips.traffic.model.TrafficDataEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Define processing of traffic data
 */
public interface TrafficDataProcessor {

    /**
     * Retrieve the number of cars seen in total
     *
     * @param trafficData list of traffic data
     * @return total number of cars
     */
    int retrieveTotalCars(List<TrafficDataEntry> trafficData);

    /**
     * Retrieve the total number of cars in each day
     *
     * @param trafficData list of traffic data
     * @return list of traffic data entries in "yyyy-mm-dd numberOfCars" format (eg. 2016-11-23 289)
     */
    Map<LocalDate, Integer> retrieveCarsByDay(List<TrafficDataEntry> trafficData);

    /**
     * The specified number of top results from the half hours with most cars
     * If there are 2 or more entries with same number of cars, then will return the occurrences until the required
     * number of results reached
     *
     * @param trafficData  list of traffic data
     * @param numOfResults number of expected top results
     * @return list of traffic data entries in "yyyy-mmddThh:mm:ss numberOfCars" format (eg. 2021-12-01T05:00:30 45)
     */
    List<TrafficDataEntry> retrieveMostCars(List<TrafficDataEntry> trafficData, int numOfResults);

    /**
     * The contiguous least car entries identified in specific time period
     * If there are 2 or more contiguous entries with same number of cars, then will return the first occurrence
     *
     * @param trafficData list of traffic data
     * @param period      time period in hours
     * @param gap         time gap between contiguous records, in hours
     * @return list of traffic data entries in "yyyy-mmddThh:mm:ss numberOfCars" format (eg. 2021-12-01T05:00:30 0)
     */
    List<TrafficDataEntry> retrieveLeastCars(List<TrafficDataEntry> trafficData, float period, float gap);
}
