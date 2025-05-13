package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.demo.dto.Event;
import com.example.demo.dto.User;
import com.example.demo.exception.EventNotFoundException;
import com.example.demo.exception.NotificationNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.feignclient.EventClient;
import com.example.demo.feignclient.UserClient;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.service.NotificationServiceImpl;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationServiceApplicationTests {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceApplicationTests.class);
    
    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private UserClient userClient;
    
    @Mock
    private EventClient eventClient;
    
    @InjectMocks
    private NotificationServiceImpl notificationService;
    
    private AutoCloseable closeable;
    private Notification testNotification;
    private User testUser;
    private Event testEvent;
    
    @BeforeAll
    void setUpTestSuite() {
        logger.info("Starting NotificationService test suite");
    }
    
    @BeforeEach
    void setup() {
        // Initialize mocks
        closeable = MockitoAnnotations.openMocks(this);
        
        // Create test user
        testUser = new User();
        testUser.setUserId(1);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        
        // Create test event
        testEvent = new Event();
        testEvent.setEventId(1);
        testEvent.setName("Test Event");
        
        // Create test notification
        testNotification = new Notification();
        testNotification.setNotificationId(1);
        testNotification.setUserId(1);
        testNotification.setEventId(1);
        testNotification.setMessage("Test notification message");
        testNotification.setTimestamp(LocalDateTime.now());
        
        logger.info("Test setup completed");
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
        logger.info("Test cleanup completed");
    }
    
    @AfterAll
    void tearDownTestSuite() {
        logger.info("Completed NotificationService test suite");
    }
    
    @Test
    @DisplayName("Send Notification - Success")
    void testSendNotification_Success() {
        // Arrange
        Notification inputNotification = new Notification();
        inputNotification.setUserId(1);
        inputNotification.setEventId(1);
        inputNotification.setMessage("Test notification");
        
        when(userClient.getUserById(anyInt())).thenReturn(testUser);
        when(eventClient.getEventById(anyInt())).thenReturn(testEvent);
        when(notificationRepository.existsByUserIdAndEventIdAndMessage(anyInt(), anyInt(), anyString()))
            .thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        
        // Act
        Notification result = notificationService.sendNotification(inputNotification);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getNotificationId());
        assertEquals("Test notification message", result.getMessage());
        
        // Verify
        verify(userClient).getUserById(1);
        verify(eventClient).getEventById(1);
        verify(notificationRepository).existsByUserIdAndEventIdAndMessage(1, 1, "Test notification");
        verify(notificationRepository).save(any(Notification.class));
    }
    
    @Test
    @DisplayName("Get All Notifications By User ID - Success")
    void testGetAllNotificationsByUserId_Success() {
        // Arrange
        List<Notification> notificationList = Arrays.asList(testNotification);
        
        when(userClient.getUserById(anyInt())).thenReturn(testUser);
        when(notificationRepository.findByUserId(anyInt())).thenReturn(notificationList);
        
        // Act
        List<Notification> result = notificationService.getAllNotificationsByUserId(1);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getNotificationId());
        
        // Verify
        verify(userClient).getUserById(1);
        verify(notificationRepository).findByUserId(1);
    }
    
    @Test
    @DisplayName("Get All Notifications By Event ID - Success")
    void testGetAllNotificationsByEventId_Success() {
        // Arrange
        List<Notification> notificationList = Arrays.asList(testNotification);
        
        when(eventClient.getEventById(anyInt())).thenReturn(testEvent);
        when(notificationRepository.findByEventId(anyInt())).thenReturn(notificationList);
        
        // Act
        List<Notification> result = notificationService.getAllNotificationsByEventId(1);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getNotificationId());
        
        // Verify
        verify(eventClient).getEventById(1);
        verify(notificationRepository).findByEventId(1);
    }
    
    @Test
    @DisplayName("Get Notification By ID - Success")
    void testGetNotificationById_Success() {
        // Arrange
        when(notificationRepository.findById(anyInt())).thenReturn(Optional.of(testNotification));
        
        // Act
        Notification result = notificationService.getNotificationById(1);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getNotificationId());
        assertEquals("Test notification message", result.getMessage());
        
        // Verify
        verify(notificationRepository).findById(1);
    }
    
    @Test
    @DisplayName("Get Notification By ID - Not Found")
    void testGetNotificationById_NotFound() {
        // Arrange
        when(notificationRepository.findById(anyInt())).thenReturn(Optional.empty());
        
        // Act & Assert
        NotificationNotFoundException exception = assertThrows(NotificationNotFoundException.class,
                () -> notificationService.getNotificationById(999));
        
        assertEquals("Notification not found with ID: 999", exception.getMessage());
        
        // Verify
        verify(notificationRepository).findById(999);
    }
    
    @Test
    @DisplayName("Send Notification - User Not Found")
    void testSendNotification_UserNotFound() {
        // Arrange
        Notification inputNotification = new Notification();
        inputNotification.setUserId(999);
        inputNotification.setEventId(1);
        inputNotification.setMessage("Test notification");
        
        when(userClient.getUserById(999)).thenReturn(null);
        
        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> notificationService.sendNotification(inputNotification));
        
        assertEquals("User not found with ID: 999", exception.getMessage());
        
        // Verify
        verify(userClient).getUserById(999);
        verify(eventClient, never()).getEventById(anyInt());
        verify(notificationRepository, never()).save(any(Notification.class));
    }
    
    @Test
    @DisplayName("Send Notification - Event Not Found")
    void testSendNotification_EventNotFound() {
        // Arrange
        Notification inputNotification = new Notification();
        inputNotification.setUserId(1);
        inputNotification.setEventId(999);
        inputNotification.setMessage("Test notification");
        
        when(userClient.getUserById(1)).thenReturn(testUser);
        when(eventClient.getEventById(999)).thenReturn(null);
        
        // Act & Assert
        EventNotFoundException exception = assertThrows(EventNotFoundException.class,
                () -> notificationService.sendNotification(inputNotification));
        
        assertEquals("Event not found with ID: 999", exception.getMessage());
        
        // Verify
        verify(userClient).getUserById(1);
        verify(eventClient).getEventById(999);
        verify(notificationRepository, never()).save(any(Notification.class));
    }
    
    @Test
    @DisplayName("Send Notification - Duplicate Notification")
    void testSendNotification_Duplicate() {
        // Arrange
        Notification inputNotification = new Notification();
        inputNotification.setUserId(1);
        inputNotification.setEventId(1);
        inputNotification.setMessage("Duplicate message");
        
        when(userClient.getUserById(1)).thenReturn(testUser);
        when(eventClient.getEventById(1)).thenReturn(testEvent);
        when(notificationRepository.existsByUserIdAndEventIdAndMessage(1, 1, "Duplicate message"))
            .thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> notificationService.sendNotification(inputNotification));
        
        assertTrue(exception.getMessage().contains("Duplicate notification already exists"));
        
        // Verify
        verify(userClient).getUserById(1);
        verify(eventClient).getEventById(1);
        verify(notificationRepository).existsByUserIdAndEventIdAndMessage(1, 1, "Duplicate message");
        verify(notificationRepository, never()).save(any(Notification.class));
    }
    
    @Test
    @DisplayName("Get All Notifications By User ID - No Notifications Found")
    void testGetAllNotificationsByUserId_NotFound() {
        // Arrange
        when(userClient.getUserById(1)).thenReturn(testUser);
        when(notificationRepository.findByUserId(1)).thenReturn(new ArrayList<>());
        
        // Act & Assert
        NotificationNotFoundException exception = assertThrows(NotificationNotFoundException.class,
                () -> notificationService.getAllNotificationsByUserId(1));
        
        assertEquals("No notifications found for user ID: 1", exception.getMessage());
        
        // Verify
        verify(userClient).getUserById(1);
        verify(notificationRepository).findByUserId(1);
    }
    
    @Test
    @DisplayName("Get All Notifications By Event ID - No Notifications Found")
    void testGetAllNotificationsByEventId_NotFound() {
        // Arrange
        when(eventClient.getEventById(1)).thenReturn(testEvent);
        when(notificationRepository.findByEventId(1)).thenReturn(new ArrayList<>());
        
        // Act & Assert
        NotificationNotFoundException exception = assertThrows(NotificationNotFoundException.class,
                () -> notificationService.getAllNotificationsByEventId(1));
        
        assertEquals("No notifications found for event ID: 1", exception.getMessage());
        
        // Verify
        verify(eventClient).getEventById(1);
        verify(notificationRepository).findByEventId(1);
    }
    
    @Test
    @DisplayName("Send Notification - Invalid Input")
    void testSendNotification_InvalidInput() {
        // Arrange - Invalid userId
        Notification invalidUserNotification = new Notification();
        invalidUserNotification.setUserId(0); // Invalid user ID
        invalidUserNotification.setEventId(1);
        invalidUserNotification.setMessage("Test message");
        
        // Arrange - Invalid eventId
        Notification invalidEventNotification = new Notification();
        invalidEventNotification.setUserId(1);
        invalidEventNotification.setEventId(0); // Invalid event ID
        invalidEventNotification.setMessage("Test message");
        
        // Arrange - Invalid message
        Notification invalidMessageNotification = new Notification();
        invalidMessageNotification.setUserId(1);
        invalidMessageNotification.setEventId(1);
        invalidMessageNotification.setMessage(""); // Empty message
        
        // Act & Assert - Invalid userId
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class,
                () -> notificationService.sendNotification(invalidUserNotification));
        
        assertTrue(exception1.getMessage().contains("Invalid input parameters"));
        
        // Act & Assert - Invalid eventId
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class,
                () -> notificationService.sendNotification(invalidEventNotification));
        
        assertTrue(exception2.getMessage().contains("Invalid input parameters"));
        
        // Act & Assert - Invalid message
        IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class,
                () -> notificationService.sendNotification(invalidMessageNotification));
        
        assertTrue(exception3.getMessage().contains("Invalid input parameters"));
        
        // Verify no interactions with remote services or repository for invalid inputs
        verify(userClient, never()).getUserById(anyInt());
        verify(eventClient, never()).getEventById(anyInt());
        verify(notificationRepository, never()).save(any(Notification.class));
    }
}