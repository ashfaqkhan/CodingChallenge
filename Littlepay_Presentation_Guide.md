# Littlepay Coding Challenge
## Overview
This application processes a stream of transit card tap events and generates a list of customer trips.

### Key Objectives
The primary objectives of the solution were:
- Correct implementation of business rules
- Low memory footprint
- Clear separation of responsibilities
- Future extensibility

## Problem Overview
Each tap represents either the start or end of a journey. The system needs to identify valid journeys, determine the correct trip status, and calculate the corresponding fare.
### Supported Trip Outcomes
- COMPLETED
- CANCELLED
- INCOMPLETE

### Fare Matrix
- Stop1 ↔ Stop2 = $3.25
- Stop2 ↔ Stop3 = $5.50
- Stop1 ↔ Stop3 = $7.30

## Assumptions
1. The input CSV file is well-formed and contains all required fields.
2. Tap records are provided in chronological order.
3. A passenger can only have one active trip at a time for a given PAN.
4. A Tap OFF always corresponds to the currently active trip for the same PAN.
5. A TAP OFF event without a corresponding active TAP ON event for the same PAN is considered INVALID input.
6. All stops referenced by input tap events are valid and supported by the configured fare matrix.
7. The fare matrix is fixed and defined by the challenge specification.
8. The first tap event encountered for a PAN must be a TAP ON event. Any first occurrence of a PAN as a TAP OFF event is considered invalid input.

## Slide 3 – Solution Architecture

```text
FareEngineApplication
        |
        v
CsvTapProcessor
        |
        v
TripProcessingService
        |
        v
FareCalculationService
```

### Responsibilities
- FareEngineApplication: Bootstrap application
- CsvTapProcessor: Read input and write output
- TripProcessingService: Manage trip lifecycle
- FareCalculationService: Calculate fares

## Slide 4 – Domain Model
I intentionally kept the domain model small and aligned with the challenge terminology. This helped keep the implementation simple and focused.
### Core Domain Objects
- Tap
- Trip
- TripFare
- TapType
- TripStatus

## Slide 5 – Streaming Processing Design

```text
Read CSV Record
        |
        v
Parse Tap
        |
        v
Process Trip
        |
        v
Write Output
        |
        v
Next Record
```

## Trip Lifecycle
### Time Complexity
```text
Time Complexity  : O(n)
Memory Complexity: O(active trips)
```
Rather than loading the entire CSV into memory, the solution processes one record at a time. This keeps memory usage low regardless of file size.
HashMap drives efficient matching, Each PAN maps to a currently active trip. This provides constant-time lookup when matching tap-on and tap-off events
```java
Map<String, Tap> activeTrips
```
### ON Flow
Store Active Trip

### OFF Flow
Find Active Trip → Create Trip → Remove Active Trip

### EOF
Remaining Active Trips → Incomplete Trips

## Fare Calculation

### Externalized Configuration
Fares are loaded from `fares.csv` at startup and cached in memory.

### Design Benefits
- No hardcoded pricing
- O(1) fare lookup
- Easy fare updates

## Trade-Offs

### Trade-Offs
| Decision | Benefit | Trade-Off |
|-----------|----------|------------|
| HashMap | Fast lookup | In-memory state |
| Streaming | Low memory | Sequential processing |
| CSV Configuration | Simple | Not dynamic |
| Single JVM | Easy deployment | No clustering |

## Testing Strategy
I prioritized testing the business rules because that is where most defects would occur. The streaming CSV layer remains thin and is validated through integration testing.

### Unit Tests
- FareCalculationServiceTest
- TripProcessingServiceTest

### Key Scenarios
- Completed Trip
- Cancelled Trip
- Incomplete Trip
- Invalid OFF Without ON
- Reverse Route Pricing
- Maximum Fare Calculation

### Integration Tests
- End-to-end CSV processing
- Input taps.csv → Output trips.csv validation

### Test Execution
```bash
mvn test
mvn verify
```
## Production Ready Roadmap

### Phase 1
Current Solution

### Phase 2
Database Persistence
Audit History

### Phase 3
Redis State Management

### Phase 4
Kafka Event Processing

### Phase 5
Distributed Services

### What Next?
- Fare Versioning
- Effective Dated Pricing
- Peak / Off-Peak Pricing
- Audit Trail
- Metrics & Dashboards
  - Health Monitoring   

## Conclusion
My goal was to build the simplest solution that satisfies the challenge requirements while maintaining good performance characteristics and clear extension points for future growth.

### Three Key Takeaways
1. Correct Business Logic
2. Efficient Streaming Processing
3. Clear Production Upgrade Path

### Thank You, your feedback is appreciated!