# AIPS Code Challenge

This application will process the traffic data generated from automated traffic counter.

## Description

An automated traffic counter sits by a road and counts the number of cars that go past.
Every half-hour the counter outputs the number of cars seen and resets the counter to zero.

This application is to manage this data which is in a file, where each line contains
a timestamp (in yyyy-mmddThh:mm:ss format, i.e. ISO 8601) for the beginning of a half-hour and the number of
cars seen that half hour. We can assume clean input, as these files are machine-generated.

## Input

Input for the application will be provided from a file.

Example:
```
2021-12-01T05:00:00 5
2021-12-01T05:30:00 12
2021-12-01T06:00:00 14
2021-12-01T06:30:00 15
2021-12-01T07:00:00 25
2021-12-01T07:30:00 46
2021-12-01T08:00:00 42
2021-12-01T15:00:00 9
2021-12-01T15:30:00 11
2021-12-01T23:30:00 0
2021-12-05T09:30:00 18
2021-12-05T10:30:00 15
2021-12-05T11:30:00 7
2021-12-05T12:30:00 6
2021-12-05T13:30:00 9
2021-12-05T14:30:00 11
2021-12-05T15:30:00 15
2021-12-08T18:00:00 33
2021-12-08T19:00:00 28
2021-12-08T20:00:00 25
2021-12-08T21:00:00 21
2021-12-08T22:00:00 16
2021-12-08T23:00:00 11
2021-12-09T00:00:00 4
```
## Run Application

1) Compile the source code

        mvn compile

2) Update TrafficData.txt with the relevant traffic data
3) Run TrafficDataAnalyzer.java while passing absolute file path of TrafficData.txt, as CLI argument
4) Run all the unit tests

        mvn test
