/*
 * Copyright (c) 2022, Indunil Rathnayake. All Rights Reserved.
 */

package com.aips.traffic.process;

import com.aips.traffic.exception.InvalidDataException;
import com.aips.traffic.exception.TrafficDataProcessException;
import com.aips.traffic.model.TrafficDataEntry;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.aips.traffic.util.TrafficDataConstants.ENTRY_SPLIT_REGEX;
import static java.text.MessageFormat.format;

/**
 * Manage processing of traffic data
 */
public class DefaultTrafficDataProcessor implements TrafficDataProcessor {

    private Logger logger = LoggerFactory.getLogger(DefaultTrafficDataProcessor.class);

    @Override
    public int retrieveTotalCars(List<TrafficDataEntry> trafficData) {

        logger.info("Retrieving total number of cars for {} traffic data entries.", trafficData.size());
        return trafficData.stream().mapToInt(dataEntry -> dataEntry.getNumOfCars()).sum();
    }

    @Override
    public Map<LocalDate, Integer> retrieveCarsByDay(List<TrafficDataEntry> trafficData) {

        logger.info("Retrieving cars by days for {} traffic data entries.", trafficData.size());
        return trafficData.stream().collect(Collectors.toMap(TrafficDataEntry::getDate, TrafficDataEntry::getNumOfCars,
                (numOfCars1, numOfCars2) -> numOfCars1 + numOfCars2, LinkedHashMap::new));
    }

    @Override
    public List<TrafficDataEntry> retrieveMostCars(List<TrafficDataEntry> trafficData, int numOfResults) {

        logger.info("Retrieving {} number of most cars for {} traffic data entries.", numOfResults, trafficData.size());
        return trafficData.stream().sorted(Comparator.comparing(TrafficDataEntry::getNumOfCars,
                Comparator.reverseOrder())).limit(numOfResults).collect(Collectors.toList());
    }

    @Override
    public List<TrafficDataEntry> retrieveLeastCars(List<TrafficDataEntry> trafficData, float period, float gap) {

        if (CollectionUtils.isEmpty(trafficData)) {
            logger.info("Traffic data is empty.");
            return Collections.EMPTY_LIST;
        }

        logger.info("Retrieving least cars in {} period with {} gap, for {} traffic data entries.",
                period, gap, trafficData.size());

        if (period % gap != 0) {
            throw new InvalidDataException(format("Provided period : [{0}] and gap : [{1}] are invalid.",
                    gap, period));
        }

        List<TrafficDataEntry> sortedTrafficData = trafficData.stream()
                .sorted(Comparator.comparing(TrafficDataEntry::getDateTime)).collect(Collectors.toList());
        AtomicInteger leastNumOfCars = new AtomicInteger(Integer.MAX_VALUE);
        AtomicInteger startOfLeast = new AtomicInteger(0);
        int contiguousNumOfRecords = Math.round(period / gap);

        Queue<TrafficDataEntry> contiguousRecordsQueue = new LinkedList<>();
        AtomicInteger pointer = new AtomicInteger(0);

        IntStream.range(0, sortedTrafficData.size() - contiguousNumOfRecords)
                .forEach(index -> retrieveStartOfLeastCars(sortedTrafficData, index, contiguousNumOfRecords,
                        leastNumOfCars, startOfLeast, gap, contiguousRecordsQueue, pointer));

        return sortedTrafficData.subList(startOfLeast.get(), startOfLeast.get() + contiguousNumOfRecords);
    }

    /**
     * Retrieve traffic data entries from the file in specified path
     *
     * @param filePath path of the file
     * @return list of traffic data entries
     */
    public List<TrafficDataEntry> retrieveTrafficDataFromFile(String filePath) {

        try (Stream<String> trafficData = Files.lines(Paths.get(filePath))) {
            return trafficData.map(
                            entry -> new TrafficDataEntry(entry.split(ENTRY_SPLIT_REGEX)[0],
                                    entry.split(ENTRY_SPLIT_REGEX)[1]))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new TrafficDataProcessException("Error when retrieving traffic data from file.", e);
        }
    }

    /**
     * Retrieve the start index of traffic data list, where the least number of cars identified
     *
     * @param trafficData            list of traffic data
     * @param index                  current index of the list
     * @param contiguousNumOfRecords number of adjacent entries to be considered
     * @param leastNumOfCars         current least number of cars
     * @param startOfLeast           current start index for least number of cars
     * @param gap                    time gap between contiguous records, in hours
     * @param contiguousRecordsQueue queue for storing the contiguous records
     * @param pointer                pointer to traverse through traffic data list
     */
    private void retrieveStartOfLeastCars(List<TrafficDataEntry> trafficData, int index, int contiguousNumOfRecords,
                                          AtomicInteger leastNumOfCars, AtomicInteger startOfLeast, float gap,
                                          Queue<TrafficDataEntry> contiguousRecordsQueue, AtomicInteger pointer) {

        if (isEntryAlreadyProcessed(index, contiguousNumOfRecords, contiguousRecordsQueue, pointer)) {
            logger.info("Traffic data in index : {} is already processed.", index);
            return;
        }

        if (CollectionUtils.isEmpty(contiguousRecordsQueue)) {
            contiguousRecordsQueue.offer(trafficData.get(index));
        }

        while (contiguousRecordsQueue.size() < contiguousNumOfRecords) {
            if (!isGapValid(trafficData, gap, contiguousRecordsQueue, pointer)) {
                return;
            }
            contiguousRecordsQueue.offer(trafficData.get(pointer.incrementAndGet()));
        }

        updateLeastNumOfCars(index, leastNumOfCars, startOfLeast, contiguousRecordsQueue);
        contiguousRecordsQueue.poll();
    }

    /**
     * Update the least number of cars
     *
     * @param index                  current index
     * @param leastNumOfCars         current least number of cars
     * @param startOfLeast           current start index for least number of cars
     * @param contiguousRecordsQueue queue for storing the contiguous records
     */
    private void updateLeastNumOfCars(int index, AtomicInteger leastNumOfCars, AtomicInteger startOfLeast,
                                      Queue<TrafficDataEntry> contiguousRecordsQueue) {

        Optional<Integer> numberOfCars = contiguousRecordsQueue.stream()
                .map(TrafficDataEntry::getNumOfCars).reduce((entry1, entry2) -> entry1 + entry2);
        if (numberOfCars.isPresent() && numberOfCars.get() < leastNumOfCars.get()) {
            leastNumOfCars.set(numberOfCars.get());
            startOfLeast.set(index);
        }
    }

    /**
     * Check whether the gap is not as expected
     *
     * @param trafficData            list of traffic data
     * @param gap                    time gap between contiguous records, in hours
     * @param contiguousRecordsQueue queue for storing the contiguous records
     * @param pointer                pointer to traverse through traffic data list
     * @return true, if the gap is valid
     */
    private boolean isGapValid(List<TrafficDataEntry> trafficData, float gap,
                               Queue<TrafficDataEntry> contiguousRecordsQueue, AtomicInteger pointer) {

        if (Duration.between(trafficData.get(pointer.get()).getDateTime(),
                trafficData.get(pointer.get() + 1).getDateTime()).toMinutes() != gap * 60) {
            pointer.incrementAndGet();
            while (!contiguousRecordsQueue.isEmpty()) {
                contiguousRecordsQueue.poll();
            }
            return false;
        }
        return true;
    }

    /**
     * Check whether an entry is already processed or not
     *
     * @param index                  current index
     * @param contiguousNumOfRecords number of adjacent entries to be considered
     * @param contiguousRecordsQueue queue for storing the contiguous records
     * @param pointer                pointer to traverse through traffic data list
     * @return true, if the entry in provided index is already processed
     */
    private boolean isEntryAlreadyProcessed(int index, int contiguousNumOfRecords,
                                            Queue<TrafficDataEntry> contiguousRecordsQueue, AtomicInteger pointer) {

        return pointer.get() != 0 && pointer.get() > index && contiguousRecordsQueue.size() < contiguousNumOfRecords - 1;
    }

    /*
    Approach 02 :

    private void retrieveStartOfLeastCars(List<TrafficDataEntry> trafficData, int index, int contiguousNumOfRecords,
                                          AtomicInteger leastNumOfCars, AtomicInteger startOfLeast, float gap) {

        int gapIncrement = 1;
        for (int i = index + 1; i < index + contiguousNumOfRecords; i++) {
            if (Duration.between(trafficData.get(index).getDateTime(),
                    trafficData.get(i).getDateTime()).toMinutes() != gapIncrement * gap * 60) {
                return;
            }
            gapIncrement++;
        }

        Optional<Integer> numberOfCars = trafficData.subList(index, index + contiguousNumOfRecords).stream()
                .map(TrafficDataEntry::getNumOfCars).reduce((entry1, entry2) -> entry1 + entry2);
        if (numberOfCars.isPresent() && numberOfCars.get() < leastNumOfCars.get()) {
            leastNumOfCars.set(numberOfCars.get());
            startOfLeast.set(index);
        }
    }*/
}
