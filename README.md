# Littlepay Coding Challenge

## Overview

This application processes a stream of transit card tap events and generates a list of customer trips.

Passengers tap their card when boarding a bus (Tap ON) and again when leaving the bus (Tap OFF). The application matches Tap ON and Tap OFF events, determines trip status, calculates fares, and produces an output CSV containing completed trip records.

The focus of the solution is correctness of trip matching, fare calculation, maintainability, and testability of the business logic.

---

## Business Rules

### Fares

| From / To | Fare |
|------------|------|
| Stop1 ↔ Stop2 | $3.25 |
| Stop2 ↔ Stop3 | $5.50 |
| Stop1 ↔ Stop3 | $7.30 |

Travel in either direction costs the same amount.

### Completed Trip

A trip is considered **COMPLETED** when:

- Passenger taps ON at one stop
- Passenger taps OFF at a different stop

Charge according to the fare matrix.

### Cancelled Trip

A trip is considered **CANCELLED** when:

- Passenger taps ON
- Passenger taps OFF at the same stop

Charge: **$0.00**

### Incomplete Trip

A trip is considered **INCOMPLETE** when:

- Passenger taps ON
- No matching OFF tap exists

The passenger is charged the maximum possible fare from the tap-on stop.

---

## Assumptions

1. The input CSV file is well-formed and contains all required fields.
2. Tap records are provided in chronological order.
3. A passenger can only have one active trip at a time for a given PAN.
4. A Tap OFF always corresponds to the currently active trip for the same PAN.
5. A TAP OFF event without a corresponding active TAP ON event for the same PAN is considered INVALID input.
6. All stops referenced by input tap events are valid and supported by the configured fare matrix.
7. Any Tap ON without a matching Tap OFF by the end of processing is treated as an INCOMPLETE trip.
8. The fare matrix is fixed and defined by the challenge specification.
9. PAN values provided are test card numbers only.
10. The first tap event encountered for a PAN must be a TAP ON event. Any first occurrence of a PAN as a TAP OFF event is considered invalid input.

---

## Solution Design

The solution separates:

- Input/Output processing
- Trip matching logic
- Fare calculation logic
- Domain models

High-level flow:

```text
Read Input CSV
        |
        v
Process Tap Events
        |
        v
Match ON/OFF Taps
        |
        v
Calculate Fare
        |
        v
Generate Trips
        |
        v
Write Output CSV
```

---

## Running the Application

### Prerequisites

- Java 17+
- Maven 3.9+

### Build

```bash
mvn clean package
```

### Run

```bash
java -jar target/transit-fare-engine-1.0.0.jar sample-data/taps.csv sample-data/trips.csv
```

---

## Running Tests

```bash
mvn test
```

`mvn test` runs the unit tests. Run `mvn verify` to run both unit and integration
tests, including a full CSV-to-trips processing scenario.

---

## Project Structure

```text
transit-fare-engine/
├── README.md
├── pom.xml
├── sample-data/
└── src/
    ├── main/
    │   ├── java/com/transit/fareengine/
    │   │   ├── FareEngineApplication.java
    │   │   ├── model/
    │   │   ├── service/
    │   │   └── exception/
    │   └── resources/
    └── test/java/com/transit/fareengine/
        ├── FareEngineApplicationTest.java
        ├── service/
        │   ├── FareCalculationServiceTest.java
        │   └── TripProcessingServiceTest.java
        └── integration/
            └── CsvTapProcessorIT.java
```

The package directories are scaffolded for the implementation; sample CSV files
will be added with the input and output formats.

---

## Future Improvements
- Externalize fare rules and routes into a configuration file or database to simplify maintenance and support fare changes without code modifications.
- Replace in-memory trip tracking with a persistent store for handling very large datasets and enabling fault tolerance.
- Increase test coverage with additional performance, boundary, and negative test scenarios.