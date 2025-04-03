package tasks.model;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;

class TaskCreationTest {

    private SimpleDateFormat sdf;
    private Date standardDate;
    private Task task;

    @BeforeEach
    void setUp() {
        sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        try {
            standardDate = sdf.parse("05.04.2024 10:00");
        } catch (ParseException e) {
            Assertions.fail("Date parsing failed: " + e.getMessage());
        }
    }

    // ======================= ECP TESTS =======================

    // Test Case 1: Create non-repeated task (endDate is null)
    @Test
    @DisplayName("ECP 1: Create non-repeated task with valid data")
    void testCreateNonRepeatedTask() {
        try {
            // Arrange
            String title = "Doing things";
            Date startDate = sdf.parse("05.04.2024 10:00");

            // Act
            Task result = new Task(title, startDate);
            result.setActive(true);

            // Assert
            Assertions.assertEquals(title, result.getTitle());
            Assertions.assertEquals(startDate, result.getStartTime());
            Assertions.assertFalse(result.isRepeated());
            Assertions.assertTrue(result.isActive());
        } catch (Exception e) {
            Assertions.fail("Should not throw exception: " + e.getMessage());
        }
    }

    // Test Case 2: Create repeated task with negative end date
    @Test
    @DisplayName("ECP 2: Create repeated task with invalid end date (negative time)")
    void testCreateRepeatedTaskWithNegativeEndDate() {
        try {
            // Arrange
            String title = "Doing things";
            Date startDate = sdf.parse("05.04.2024 10:00");
            Date endDate = new Date(-1); // Negative time
            int interval = 3;

            // Act & Assert
            Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new Task(title, startDate, endDate, interval);
            });

            Assertions.assertTrue(exception.getMessage().contains("negative") ||
                    exception.getMessage().contains("Time cannot be negative"));
        } catch (ParseException e) {
            Assertions.fail("Date parsing failed: " + e.getMessage());
        }
    }

    // Test Case 3: Create repeated task with valid parameters but end before start
    @Test
    @DisplayName("ECP 3: Create repeated task with end date before start date")
    void testCreateRepeatedTaskWithEndBeforeStart() {
        try {
            // Arrange
            String title = "Doing things";
            Date startDate = sdf.parse("05.04.2024 10:00");
            Date endDate = sdf.parse("05.04.2024 08:00"); // Before start
            int interval = 3;

            // Act & Assert
            Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new Task(title, startDate, endDate, interval);
            });

            Assertions.assertTrue(exception.getMessage().contains("Start date should be before end"));
        } catch (ParseException e) {
            Assertions.fail("Date parsing failed: " + e.getMessage());
        }
    }

    // Test Case 4: Create repeated task with invalid interval
    @Test
    @DisplayName("ECP 4: Create repeated task with invalid interval")
    void testCreateRepeatedTaskWithInvalidInterval() {
        try {
            // Arrange
            String title = "Doing things";
            Date startDate = sdf.parse("05.04.2024 10:00");
            Date endDate = sdf.parse("05.04.2024 12:00");
            int interval = 0; // Invalid interval

            // Act & Assert
            Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new Task(title, startDate, endDate, interval);
            });

            Assertions.assertTrue(exception.getMessage().contains("interval") ||
                    exception.getMessage().contains("should"));
        } catch (ParseException e) {
            Assertions.fail("Date parsing failed: " + e.getMessage());
        }
    }

    // Test Case 5: Create repeated task with valid data
    @Test
    @DisplayName("ECP 5: Create repeated task with valid data")
    void testCreateRepeatedTaskWithValidData() {
        try {
            // Arrange
            String title = "Doing things";
            Date startDate = sdf.parse("05.04.2024 10:00");
            Date endDate = sdf.parse("05.04.2024 12:00");
            int interval = 3;

            // Act
            Task result = new Task(title, startDate, endDate, interval);
            result.setActive(true);

            // Assert
            Assertions.assertEquals(title, result.getTitle());
            Assertions.assertEquals(startDate, result.getStartTime());
            Assertions.assertEquals(endDate, result.getEndTime());
            Assertions.assertEquals(interval, result.getRepeatInterval());
            Assertions.assertTrue(result.isRepeated());
            Assertions.assertTrue(result.isActive());
        } catch (Exception e) {
            Assertions.fail("Should not throw exception: " + e.getMessage());
        }
    }

    // Test Case 6: Create repeated task with end == start
    @Test
    @DisplayName("ECP 6: Create repeated task with end date equal to start date")
    void testCreateRepeatedTaskWithEndEqualStart() {
        try {
            // Arrange
            String title = "Doing things";
            Date startDate = sdf.parse("05.04.2024 10:00");
            Date endDate = sdf.parse("05.04.2024 10:00"); // Same as start
            int interval = 3;

            // Act & Assert
            Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new Task(title, startDate, endDate, interval);
            });

            Assertions.assertTrue(exception.getMessage().contains("Start date should be before end"));
        } catch (ParseException e) {
            Assertions.fail("Date parsing failed: " + e.getMessage());
        }
    }

    // ======================= BVA TESTS =======================

    // Test Case 1: Boundary condition for start date = end date - 1 minute
    @Test
    @DisplayName("BVA 1: Start date is just before end date")
    void testStartDateJustBeforeEndDate() {
        try {
            // Arrange
            String title = "Doing things";
            Date startDate = sdf.parse("05.04.2024 10:00");
            Date endDate = sdf.parse("05.04.2024 10:01"); // Just 1 minute after start
            int interval = 3;

            // Act
            Task result = new Task(title, startDate, endDate, interval);

            // Assert
            Assertions.assertEquals(startDate, result.getStartTime());
            Assertions.assertEquals(endDate, result.getEndTime());
        } catch (Exception e) {
            Assertions.fail("Should not throw exception: " + e.getMessage());
        }
    }

    // Test Case 2: Minimum valid interval
    @Test
    @DisplayName("BVA 2: Minimum valid interval value (1)")
    void testMinimumValidInterval() {
        try {
            // Arrange
            String title = "Doing things";
            Date startDate = sdf.parse("05.04.2024 10:00");
            Date endDate = sdf.parse("05.04.2024 11:00");
            int interval = 1; // Minimum valid

            // Act
            Task result = new Task(title, startDate, endDate, interval);

            // Assert
            Assertions.assertEquals(interval, result.getRepeatInterval());
        } catch (Exception e) {
            Assertions.fail("Should not throw exception: " + e.getMessage());
        }
    }

    // Test Case 3: Boundary for zero interval
    @Test
    @DisplayName("BVA 3: Interval value of zero (invalid)")
    void testZeroInterval() {
        try {
            // Arrange
            String title = "Doing things";
            Date startDate = sdf.parse("05.04.2024 10:00");
            Date endDate = sdf.parse("05.04.2024 11:00");
            int interval = 0; // Invalid

            // Act & Assert
            Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new Task(title, startDate, endDate, interval);
            });

            Assertions.assertTrue(exception.getMessage().contains("interval") ||
                    exception.getMessage().contains("should be > 1"));
        } catch (ParseException e) {
            Assertions.fail("Date parsing failed: " + e.getMessage());
        }
    }

    // Test Case 4: Negative time values
    @Test
    @DisplayName("BVA 4: Negative time values")
    void testNegativeTimeValues() {
        // Arrange
        String title = "Doing things";
        Date startDate = new Date(-1); // Negative time

        // Act & Assert
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Task(title, startDate);
        });

        Assertions.assertTrue(exception.getMessage().contains("negative") ||
                exception.getMessage().contains("Time cannot be negative"));
    }

    // Additional test using parameterized testing for different interval values
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 60, 1440}) // Various valid intervals
    @DisplayName("Parameterized: Valid interval values")
    void testValidIntervals(int interval) {
        try {
            // Arrange
            String title = "Doing things";
            Date startDate = sdf.parse("05.04.2024 10:00");
            Date endDate = sdf.parse("05.04.2024 11:00");

            // Act
            Task result = new Task(title, startDate, endDate, interval);

            // Assert
            Assertions.assertEquals(interval, result.getRepeatInterval());
        } catch (Exception e) {
            Assertions.fail("Should not throw exception with interval " + interval + ": " + e.getMessage());
        }
    }

    // Nested test class for time-related validations
    @Nested
    @DisplayName("Time validation tests")
    class TimeValidationTests {

        @Test
        @DisplayName("Test date cannot be negative")
        void testDateCannotBeNegative() {
            // Arrange
            String title = "Doing things";
            Date negativeDate = new Date(-1);

            // Act & Assert
            Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new Task(title, negativeDate);
            });

            Assertions.assertTrue(exception.getMessage().contains("Time cannot be negative"));
        }
    }

    @AfterEach
    void tearDown() {
        task = null;
    }
}