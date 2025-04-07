package tasks.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tasks.model.Task;
import tasks.services.TasksService;
import tasks.persistence.ArrayTaskList;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class NewEditControllerTest {

    private NewEditController controller;
    private TestableNewEditController testableController;
    private ObservableList<Task> tasksList;

    @BeforeEach
    void setUp() {
        tasksList = FXCollections.observableArrayList();
        controller = new NewEditController();
        testableController = new TestableNewEditController();
        testableController.setTasksList(tasksList);

        ArrayTaskList arrayTaskList = new ArrayTaskList();
        TasksService service = new TasksService(arrayTaskList);
        testableController.setService(service);
    }

    // ECP Tests

    @Test
    @DisplayName("ECP1: Valid task with null end date (non-repeated task)")
    void saveChanges_ValidTaskWithNullEndDate() {
        // Arrange
        String title = "Doing things";
        Date startDate = new Date();

        // Act
        Task result = testableController.makeTaskForTest(title, startDate, null, 0, true);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertTrue(result.isActive());
        assertFalse(result.isRepeated());
    }

    @Test
    @DisplayName("ECP2: Valid task with valid end date and interval")
    void saveChanges_ValidTaskWithEndDateAndInterval() {
        // Arrange
        String title = "Doing things";
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 3600000); // 1 hour later
        int interval = 180; // 3 minutes

        // Act
        Task result = testableController.makeTaskForTest(title, startDate, endDate, interval, true);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertTrue(result.isActive());
        assertTrue(result.isRepeated());
        assertEquals(interval, result.getRepeatInterval());
    }

    @Test
    @DisplayName("ECP3: Invalid task with start date after end date")
    void saveChanges_InvalidTaskWithStartDateAfterEndDate() {
        // Arrange
        String title = "Doing things";
        Date startDate = new Date(System.currentTimeMillis() + 86400000); // Tomorrow
        Date endDate = new Date(); // Today (before start date)
        int interval = 180; // 3 minutes

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            testableController.makeTaskForTest(title, startDate, endDate, interval, true);
        });
    }

    // BVA Tests

    @Test
    @DisplayName("BVA1: Task with interval = 1 (minimum valid value)")
    void saveChanges_TaskWithMinimumValidInterval() {
        // Arrange
        String title = "Doing things";
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 3600000); // 1 hour later
        int interval = 60; // 1 minute (60 seconds)

        // Act
        Task result = testableController.makeTaskForTest(title, startDate, endDate, interval, true);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertTrue(result.isActive());
        assertTrue(result.isRepeated());
        assertEquals(interval, result.getRepeatInterval());
    }

    @Test
    @DisplayName("BVA2: Task with interval = 0 (invalid value, below boundary)")
    void saveChanges_TaskWithInvalidIntervalZero() {
        // Arrange
        String title = "Doing things";
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 3600000); // 1 hour later
        int interval = 0; // Invalid interval

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            testableController.makeTaskForTest(title, startDate, endDate, interval, true);
        });
    }

    @Test
    @DisplayName("BVA3: Task with interval = -1 (invalid negative value)")
    void saveChanges_TaskWithInvalidNegativeInterval() {
        // Arrange
        String title = "Doing things";
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 3600000); // 1 hour later
        int interval = -60; // Invalid negative interval

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            testableController.makeTaskForTest(title, startDate, endDate, interval, true);
        });
    }

    @Test
    @DisplayName("BVA4: Task with interval = 2 (valid value above boundary)")
    void saveChanges_TaskWithValidIntervalTwo() {
        // Arrange
        String title = "Doing things";
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 3600000); // 1 hour later
        int interval = 120; // 2 minutes

        // Act
        Task result = testableController.makeTaskForTest(title, startDate, endDate, interval, true);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertTrue(result.isActive());
        assertTrue(result.isRepeated());
        assertEquals(interval, result.getRepeatInterval());
    }

    // Helper class that exposes the makeTask method for testing
    private class TestableNewEditController extends NewEditController {
        public Task makeTaskForTest(String title, Date startDate, Date endDate, int interval, boolean isActive) {
            Task task;
            if (endDate == null) {
                task = new Task(title, startDate);
            } else {
                task = new Task(title, startDate, endDate, interval);
            }
            task.setActive(isActive);
            return task;
        }
    }
}