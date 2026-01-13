# Unit Tests Documentation

## Overview
This directory contains comprehensive unit tests for the TrekSathi payment management system.

## Test Structure

### 1. Service Tests (`service/OrganizerPaymentServiceTest.java`)
Tests for the `OrganizerPaymentService` business logic layer.

**Test Coverage:**
- ✅ Get payment dashboard successfully
- ✅ Handle organizer not found exception
- ✅ Filter payments by status (COMPLETED, PENDING, etc.)
- ✅ Filter payments by event ID
- ✅ Filter payments by date range
- ✅ Filter payments by payment method
- ✅ Calculate payment summary correctly
- ✅ Build event payments correctly
- ✅ Build participant payments correctly
- ✅ Build revenue chart for last 6 months
- ✅ Handle empty payments list
- ✅ Calculate monthly growth correctly

**Key Test Scenarios:**
- Valid dashboard retrieval with all filters
- Exception handling for missing organizer
- Empty data handling
- Payment status filtering
- Date range filtering
- Payment method filtering

### 2. Controller Tests (`controller/OrganizerPaymentControllerTest.java`)
Tests for the `OrganizerPaymentController` REST API endpoints.

**Test Coverage:**
- ✅ GET `/organizer/payments/dashboard/{organizerId}` - Success case
- ✅ GET with query parameters (fromDate, toDate, status, eventId, paymentMethod)
- ✅ Default date range when not provided
- ✅ Default status (ALL) when not provided
- ✅ POST `/organizer/payments/dashboard/{organizerId}` with filters
- ✅ Handle null filters and set defaults
- ✅ Partial date range handling
- ✅ Invalid organizer ID handling

**Key Test Scenarios:**
- REST endpoint success responses
- Query parameter parsing
- Default value assignment
- Request body validation
- Error handling

### 3. Repository Tests (`repository/PaymentRepositoryTest.java`)
Integration tests for the `PaymentRepository` data access layer.

**Test Coverage:**
- ✅ Find payment by transaction UUID
- ✅ Find payments by organizer ID
- ✅ Calculate total earnings by organizer and status
- ✅ Filter payments with multiple criteria
- ✅ Filter by event ID
- ✅ Filter by payment method
- ✅ Filter by date range

**Key Test Scenarios:**
- Basic CRUD operations
- Complex query filtering
- Aggregation queries
- Edge cases (empty results, null values)

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=OrganizerPaymentServiceTest
mvn test -Dtest=OrganizerPaymentControllerTest
mvn test -Dtest=PaymentRepositoryTest
```

### Run with Coverage
```bash
mvn test jacoco:report
```

## Test Dependencies

The tests use:
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions
- **Spring Boot Test** - Spring testing utilities
- **TestEntityManager** - JPA testing utilities

## Test Data Setup

Each test class uses `@BeforeEach` to set up test data:
- User entities
- Organizer entities
- Event entities
- EventRegistration entities
- Payment entities
- EventParticipants entities

## Best Practices

1. **Isolation**: Each test is independent and doesn't rely on other tests
2. **Mocking**: External dependencies are mocked to ensure unit test isolation
3. **Assertions**: Using AssertJ for readable and comprehensive assertions
4. **Naming**: Test methods follow the pattern: `testMethodName_Scenario_ExpectedResult`
5. **Coverage**: Tests cover both happy paths and edge cases

## Future Enhancements

- Add performance tests for large datasets
- Add integration tests with real database
- Add contract tests for API endpoints
- Add mutation testing
- Add test coverage reports

