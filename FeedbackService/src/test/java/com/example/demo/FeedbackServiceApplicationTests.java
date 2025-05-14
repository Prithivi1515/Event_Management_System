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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.demo.dto.Event;
import com.example.demo.dto.User;
import com.example.demo.exception.EventNotFoundException;
import com.example.demo.exception.FeedbackNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.feignclient.EventClient;
import com.example.demo.feignclient.UserClient;
import com.example.demo.model.Feedback;
import com.example.demo.repository.FeedbackRepository;
import com.example.demo.service.FeedbackServiceImpl;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FeedbackServiceApplicationTests {
    
    private static final Logger logger = LoggerFactory.getLogger(FeedbackServiceApplicationTests.class);
    
    @Mock
    private FeedbackRepository feedbackRepository;
    
    @Mock
    private EventClient eventClient;
    
    @Mock
    private UserClient userClient;
    
    private FeedbackServiceImpl feedbackService;
    
    private AutoCloseable closeable;
    private User testUser;
    private Event testEvent;
    private Feedback testFeedback;
    
    @BeforeAll
    void setUpTestSuite() {
        logger.info("Starting FeedbackService test suite");
    }
    
    @BeforeEach
    void setUp() {
        // Initialize mocks manually to get access to the AutoCloseable
        closeable = MockitoAnnotations.openMocks(this);
        
        // Manually create the service instance with constructor parameters in the correct order
        feedbackService = new FeedbackServiceImpl(feedbackRepository, eventClient, userClient);
        
        // Create test user
        testUser = new User();
        testUser.setUserId(1);
        testUser.setName("John Doe");
        
        // Create test event
        testEvent = new Event();
        testEvent.setEventId(1);
        testEvent.setName("TechFest");
        
        // Create test feedback
        testFeedback = new Feedback();
        testFeedback.setFeedbackId(1);
        testFeedback.setUserId(1);
        testFeedback.setEventId(1);
        testFeedback.setRating(5);
        testFeedback.setComments("Great event!");
        testFeedback.setTimestamp(LocalDateTime.now());
        
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
        logger.info("Completed FeedbackService test suite");
    }
    
    @Test
    @DisplayName("Save Feedback - Success")
    void testSaveFeedbackSuccess() {
        // Arrange
        Feedback inputFeedback = new Feedback();
        inputFeedback.setUserId(1);
        inputFeedback.setEventId(1);
        inputFeedback.setRating(5);
        inputFeedback.setComments("Good");
        
        when(userClient.getUserById(1)).thenReturn(testUser);
        when(eventClient.getEventById(1)).thenReturn(testEvent);
        when(feedbackRepository.existsByUserIdAndEventId(1, 1)).thenReturn(false);
        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> {
            Feedback savedFeedback = invocation.getArgument(0);
            savedFeedback.setFeedbackId(1); // Simulate DB assigning ID
            return savedFeedback;
        });
        
        // Act
        Feedback saved = feedbackService.saveFeedback(inputFeedback);
        
        // Assert
        assertNotNull(saved);
        assertEquals(1, saved.getFeedbackId());
        assertEquals("Good", saved.getComments());
        assertEquals(5, saved.getRating());
        assertNotNull(saved.getTimestamp());
        
        // Verify interactions
        verify(userClient).getUserById(1);
        verify(eventClient).getEventById(1);
        verify(feedbackRepository).existsByUserIdAndEventId(1, 1);
        verify(feedbackRepository).save(any(Feedback.class));
    }
    
    @Test
    @DisplayName("Save Feedback - User Not Found")
    void testSaveFeedback_UserNotFound() {
        // Arrange
        Feedback inputFeedback = new Feedback();
        inputFeedback.setUserId(1);
        inputFeedback.setEventId(1);
        inputFeedback.setRating(5);
        inputFeedback.setComments("Good");
        
        when(userClient.getUserById(1)).thenReturn(null);
        
        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, 
                () -> feedbackService.saveFeedback(inputFeedback));
        
        assertEquals("User not found with ID: 1", exception.getMessage());
        
        // Verify interactions
        verify(userClient).getUserById(1);
        verify(eventClient, never()).getEventById(anyInt());
        verify(feedbackRepository, never()).save(any(Feedback.class));
    }
    
    @Test
    @DisplayName("Save Feedback - Event Not Found")
    void testSaveFeedback_EventNotFound() {
        // Arrange
        Feedback inputFeedback = new Feedback();
        inputFeedback.setUserId(1);
        inputFeedback.setEventId(1);
        inputFeedback.setRating(5);
        inputFeedback.setComments("Good");
        
        when(userClient.getUserById(1)).thenReturn(testUser);
        when(eventClient.getEventById(1)).thenReturn(null);
        when(feedbackRepository.existsByUserIdAndEventId(1, 1)).thenReturn(false);
        
        // Act & Assert
        EventNotFoundException exception = assertThrows(EventNotFoundException.class, 
                () -> feedbackService.saveFeedback(inputFeedback));
        
        assertEquals("Event not found with ID: 1", exception.getMessage());
        
        // Verify interactions
        verify(userClient).getUserById(1);
        verify(eventClient).getEventById(1);
        verify(feedbackRepository).existsByUserIdAndEventId(1, 1);
        verify(feedbackRepository, never()).save(any(Feedback.class));
    }
    
    @Test
    @DisplayName("Save Feedback - Duplicate Feedback")
    void testSaveFeedback_DuplicateFeedback() {
        // Arrange
        Feedback inputFeedback = new Feedback();
        inputFeedback.setUserId(1);
        inputFeedback.setEventId(1);
        inputFeedback.setRating(5);
        inputFeedback.setComments("Good");
        
        when(userClient.getUserById(1)).thenReturn(testUser);
        when(feedbackRepository.existsByUserIdAndEventId(1, 1)).thenReturn(true);
        
        // Act & Assert
        FeedbackNotFoundException exception = assertThrows(FeedbackNotFoundException.class, 
                () -> feedbackService.saveFeedback(inputFeedback));
        
        assertEquals("User has already given feedback for this event.", exception.getMessage());
        
        // Verify interactions
        verify(userClient).getUserById(1);
        verify(feedbackRepository).existsByUserIdAndEventId(1, 1);
        verify(eventClient, never()).getEventById(anyInt());
        verify(feedbackRepository, never()).save(any(Feedback.class));
    }
    
    @Test
    @DisplayName("Update Feedback - Success")
    void testUpdateFeedbackSuccess() {
        // Arrange
        Feedback existing = new Feedback();
        existing.setFeedbackId(1);
        existing.setUserId(1);
        existing.setEventId(1);
        existing.setRating(4);
        existing.setComments("Ok");
        
        Feedback updated = new Feedback();
        updated.setRating(5);
        updated.setComments("Excellent");
        
        when(feedbackRepository.findById(1)).thenReturn(Optional.of(existing));
        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Feedback result = feedbackService.updateFeedback(1, updated);
        
        // Assert
        assertNotNull(result);
        assertEquals("Excellent", result.getComments());
        assertEquals(5, result.getRating());
        
        // Verify
        verify(feedbackRepository).findById(1);
        verify(feedbackRepository).save(any(Feedback.class));
        
        // Verify that only comments and rating were updated
        ArgumentCaptor<Feedback> feedbackCaptor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackRepository).save(feedbackCaptor.capture());
        Feedback capturedFeedback = feedbackCaptor.getValue();
        
        assertEquals(1, capturedFeedback.getFeedbackId());
        assertEquals(1, capturedFeedback.getUserId());
        assertEquals(1, capturedFeedback.getEventId());
        assertEquals(5, capturedFeedback.getRating());
        assertEquals("Excellent", capturedFeedback.getComments());
    }
    
    @Test
    @DisplayName("Update Feedback - Not Found")
    void testUpdateFeedback_NotFound() {
        // Arrange
        Feedback updated = new Feedback();
        updated.setRating(5);
        updated.setComments("Excellent");
        
        when(feedbackRepository.findById(999)).thenReturn(Optional.empty());
        
        // Act & Assert
        FeedbackNotFoundException exception = assertThrows(FeedbackNotFoundException.class, 
                () -> feedbackService.updateFeedback(999, updated));
        
        assertEquals("Feedback not found with ID: 999", exception.getMessage());
        
        // Verify
        verify(feedbackRepository).findById(999);
        verify(feedbackRepository, never()).save(any(Feedback.class));
    }
    
    @Test
    @DisplayName("Delete Feedback - Success")
    void testDeleteFeedbackSuccess() {
        // Arrange
        when(feedbackRepository.findById(1)).thenReturn(Optional.of(testFeedback));
        doNothing().when(feedbackRepository).delete(any(Feedback.class));
        
        // Act
        String result = feedbackService.deleteFeedback(1);
        
        // Assert
        assertEquals("Feedback deleted successfully!", result);
        
        // Verify
        verify(feedbackRepository).findById(1);
        verify(feedbackRepository).delete(testFeedback);
    }
    
    @Test
    @DisplayName("Delete Feedback - Not Found")
    void testDeleteFeedback_NotFound() {
        // Arrange
        when(feedbackRepository.findById(999)).thenReturn(Optional.empty());
        
        // Act & Assert
        FeedbackNotFoundException exception = assertThrows(FeedbackNotFoundException.class, 
                () -> feedbackService.deleteFeedback(999));
        
        assertEquals("Feedback not found with ID: 999", exception.getMessage());
        
        // Verify
        verify(feedbackRepository).findById(999);
        verify(feedbackRepository, never()).delete(any(Feedback.class));
    }
    
    @Test
    @DisplayName("Get Feedback By ID - Success")
    void testGetFeedbackById_Success() {
        // Arrange
        when(feedbackRepository.findById(1)).thenReturn(Optional.of(testFeedback));
        
        // Act
        Feedback result = feedbackService.getFeedbackById(1);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getFeedbackId());
        assertEquals(5, result.getRating());
        assertEquals("Great event!", result.getComments());
        
        // Verify
        verify(feedbackRepository).findById(1);
    }
    
    @Test
    @DisplayName("Get All Feedbacks By User - Success")
    void testGetAllFeedbacksByUser_Success() {
        // Arrange
        List<Feedback> feedbackList = Arrays.asList(testFeedback, new Feedback());
        when(feedbackRepository.findByUserId(1)).thenReturn(feedbackList);
        
        // Act
        List<Feedback> result = feedbackService.getAllFeedbacksByUser(1);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify
        verify(feedbackRepository).findByUserId(1);
    }
    
    @Test
    @DisplayName("Get All Feedbacks By User - No Feedbacks")
    void testGetAllFeedbacksByUser_NoFeedbacks() {
        // Arrange
        when(feedbackRepository.findByUserId(1)).thenReturn(new ArrayList<>());
        
        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, 
                () -> feedbackService.getAllFeedbacksByUser(1));
        
        assertEquals("No feedback found for user with ID: 1", exception.getMessage());
        
        // Verify
        verify(feedbackRepository).findByUserId(1);
    }
    
    @Test
    @DisplayName("Get All Feedbacks By Event - Success")
    void testGetAllFeedbacksByEvent_Success() {
        // Arrange
        List<Feedback> feedbackList = Arrays.asList(testFeedback, new Feedback());
        when(feedbackRepository.findByEventId(1)).thenReturn(feedbackList);
        
        // Act
        List<Feedback> result = feedbackService.getAllFeedbacksByEvent(1);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify
        verify(feedbackRepository).findByEventId(1);
    }
    
    @Test
    @DisplayName("Get Average Rating By Event - Success")
    void testGetAverageRatingByEvent_Success() {
        // Arrange
        when(feedbackRepository.findAverageRatingByEventId(1)).thenReturn(4.8);
        
        // Act
        float avgRating = feedbackService.getAverageRatingByEvent(1);
        
        // Assert
        assertEquals(4.8f, avgRating);
        
        // Verify
        verify(feedbackRepository).findAverageRatingByEventId(1);
    }
    
    @Test
    @DisplayName("Get Average Rating By Event - No Feedbacks")
    void testGetAverageRatingByEvent_NoFeedbacks() {
        // Arrange
        when(feedbackRepository.findAverageRatingByEventId(1)).thenReturn(0.0);
        
        // Act & Assert
        EventNotFoundException exception = assertThrows(EventNotFoundException.class, 
                () -> feedbackService.getAverageRatingByEvent(1));
        
        assertEquals("No feedback available for the event with ID: 1", exception.getMessage());
        
        // Verify
        verify(feedbackRepository).findAverageRatingByEventId(1);
    }
    
}