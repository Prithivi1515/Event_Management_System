package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.demo.dto.Event;
import com.example.demo.dto.NotificationRequest;
import com.example.demo.dto.User;
import com.example.demo.exception.EventNotFoundException;
import com.example.demo.exception.TicketNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.feignclient.EventClient;
import com.example.demo.feignclient.NotificationClient;
import com.example.demo.feignclient.UserClient;
import com.example.demo.model.Ticket;
import com.example.demo.model.Ticket.Status;
import com.example.demo.repository.TicketRepository;
import com.example.demo.service.TicketServiceImpl;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TicketServiceApplicationTests {
    
    private static final Logger logger = LoggerFactory.getLogger(TicketServiceApplicationTests.class);

    @Mock
    private TicketRepository ticketRepository;
    
    @Mock
    private UserClient userClient;
    
    @Mock
    private EventClient eventClient;
    
    @Mock
    private NotificationClient notificationClient;
    
    @InjectMocks
    private TicketServiceImpl ticketService;
    
    private AutoCloseable closeable;
    private Ticket ticket;
    private User user;
    private Event event;
    
    @BeforeAll
    void setUpTestSuite() {
        logger.info("Starting TicketService test suite");
    }
    
    @BeforeEach
    void setUp() {
        // Initialize mocks manually to get access to the AutoCloseable
        closeable = MockitoAnnotations.openMocks(this);
        
        // Create test data
        ticket = new Ticket();
        ticket.setTicketId(1);
        ticket.setUserId(10);
        ticket.setEventId(20);
        ticket.setBookingDate(LocalDateTime.now());
        ticket.setStatus(Status.BOOKED);
        
        user = new User();
        user.setUserId(10);
        user.setName("Test User");
        user.setEmail("test@example.com");
        
        event = new Event();
        event.setEventId(20);
        event.setName("Test Event");
        event.setTicketCount(10);
        
        // Reset mocks to clear any previous interactions
        reset(ticketRepository, userClient, eventClient, notificationClient);
        
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
        logger.info("Completed TicketService test suite");
    }
    
    @Test
    @DisplayName("Book Ticket - Success")
    void testBookTicketSuccessfully() {
        // Arrange
        logger.info("Testing successful ticket booking");
        
        // Setup necessary mocks
        when(userClient.getUserById(anyInt())).thenReturn(user);
        when(eventClient.getEventById(anyInt())).thenReturn(event);
        when(ticketRepository.existsByUserIdAndEventId(anyInt(), anyInt())).thenReturn(false);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        doNothing().when(eventClient).decreaseTicketCount(anyInt());
        doNothing().when(notificationClient).sendNotification(any(NotificationRequest.class));
        
        // Act
        Ticket booked = ticketService.bookTicket(ticket);
        
        // Assert
        assertNotNull(booked);
        assertEquals(1, booked.getTicketId());
        assertEquals(Status.BOOKED, booked.getStatus());
        
        // Verify interactions
        verify(userClient).getUserById(10);
        verify(eventClient).getEventById(20);
        verify(ticketRepository).existsByUserIdAndEventId(10, 20);
        verify(ticketRepository).save(any(Ticket.class));
        verify(eventClient).decreaseTicketCount(20);
        verify(notificationClient).sendNotification(any(NotificationRequest.class));
        
        logger.info("Book ticket test completed successfully");
    }
    
    @Test
    @DisplayName("Book Ticket - User Not Found")
    void testBookTicket_UserNotFound() {
        // Arrange
        logger.info("Testing ticket booking with non-existent user");
        
        when(userClient.getUserById(anyInt())).thenReturn(null);
        
        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, 
                () -> ticketService.bookTicket(ticket));
        
        assertEquals("User not found with ID: " + ticket.getUserId(), exception.getMessage());
        
        // Verify interactions
        verify(userClient).getUserById(10);
        verify(eventClient, never()).getEventById(anyInt());
        verify(ticketRepository, never()).save(any(Ticket.class));
        
        logger.info("User not found test completed successfully");
    }
    
    @Test
    @DisplayName("Book Ticket - Event Not Found")
    void testBookTicket_EventNotFound() {
        // Arrange
        logger.info("Testing ticket booking with non-existent event");
        
        when(userClient.getUserById(anyInt())).thenReturn(user);
        when(eventClient.getEventById(anyInt())).thenReturn(null);
        
        // Act & Assert
        EventNotFoundException exception = assertThrows(EventNotFoundException.class, 
                () -> ticketService.bookTicket(ticket));
        
        assertEquals("Event not found with ID: " + ticket.getEventId(), exception.getMessage());
        
        // Verify interactions
        verify(userClient).getUserById(10);
        verify(eventClient).getEventById(20);
        verify(ticketRepository, never()).save(any(Ticket.class));
        
        logger.info("Event not found test completed successfully");
    }
    
    @Test
    @DisplayName("Book Ticket - No Tickets Available")
    void testBookTicket_NoTicketsAvailable() {
        // Arrange
        logger.info("Testing ticket booking with no available tickets");
        
        event.setTicketCount(0); // Set ticket count to 0
        
        when(userClient.getUserById(anyInt())).thenReturn(user);
        when(eventClient.getEventById(anyInt())).thenReturn(event);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> ticketService.bookTicket(ticket));
        
        assertEquals("No tickets available for event ID: " + ticket.getEventId(), exception.getMessage());
        
        // Verify interactions
        verify(userClient).getUserById(10);
        verify(eventClient).getEventById(20);
        verify(ticketRepository, never()).save(any(Ticket.class));
        
        logger.info("No tickets available test completed successfully");
    }
    
    @Test
    @DisplayName("Book Ticket - Duplicate Booking")
    void testBookTicket_DuplicateBooking() {
        // Arrange
        logger.info("Testing duplicate ticket booking");
        
        when(userClient.getUserById(anyInt())).thenReturn(user);
        when(eventClient.getEventById(anyInt())).thenReturn(event);
        when(ticketRepository.existsByUserIdAndEventId(anyInt(), anyInt())).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> ticketService.bookTicket(ticket));
        
        assertEquals("Ticket already booked for user ID: " + ticket.getUserId() + 
                    " and event ID: " + ticket.getEventId(), exception.getMessage());
        
        // Verify interactions
        verify(userClient).getUserById(10);
        verify(eventClient).getEventById(20);
        verify(ticketRepository).existsByUserIdAndEventId(10, 20);
        verify(ticketRepository, never()).save(any(Ticket.class));
        
        logger.info("Duplicate booking test completed successfully");
    }
    
    @Test
    @DisplayName("Get Ticket By ID - Success")
    void testGetTicketById_Success() {
        // Arrange
        logger.info("Testing get ticket by ID");
        
        when(ticketRepository.findById(anyInt())).thenReturn(Optional.of(ticket));
        
        // Act
        Ticket result = ticketService.getTicketById(1);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTicketId());
        assertEquals(10, result.getUserId());
        assertEquals(20, result.getEventId());
        
        // Verify interactions
        verify(ticketRepository).findById(1);
        
        logger.info("Get ticket by ID test completed successfully");
    }
    
    @Test
    @DisplayName("Get Ticket By ID - Not Found")
    void testGetTicketById_NotFound() {
        // Arrange
        logger.info("Testing get non-existent ticket");
        
        when(ticketRepository.findById(anyInt())).thenReturn(Optional.empty());
        
        // Act & Assert
        TicketNotFoundException exception = assertThrows(TicketNotFoundException.class, 
                () -> ticketService.getTicketById(999));
        
        assertEquals("Ticket not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(ticketRepository).findById(999);
        
        logger.info("Ticket not found test completed successfully");
    }
    
    @Test
    @DisplayName("Cancel Ticket - Success")
    void testCancelTicket_Success() {
        // Arrange
        logger.info("Testing cancel ticket");
        
        when(ticketRepository.findById(anyInt())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket savedTicket = invocation.getArgument(0);
            return savedTicket; // Return the modified ticket
        });
        doNothing().when(eventClient).increaseTicketCount(anyInt());
        doNothing().when(notificationClient).sendNotification(any(NotificationRequest.class));
        
        // Act
        Ticket canceled = ticketService.cancelTicket(1);
        
        // Assert
        assertNotNull(canceled);
        assertEquals(Status.CANCELLED, canceled.getStatus());
        
        // Verify interactions
        verify(ticketRepository).findById(1);
        verify(eventClient).increaseTicketCount(20);
        verify(ticketRepository).save(any(Ticket.class));
        verify(notificationClient).sendNotification(any(NotificationRequest.class));
        
        logger.info("Cancel ticket test completed successfully");
    }
    
    @Test
    @DisplayName("Cancel Ticket - Already Cancelled")
    void testCancelTicket_AlreadyCancelled() {
        // Arrange
        logger.info("Testing cancellation of already cancelled ticket");
        
        ticket.setStatus(Status.CANCELLED); // Set ticket as already cancelled
        
        when(ticketRepository.findById(anyInt())).thenReturn(Optional.of(ticket));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> ticketService.cancelTicket(1));
        
        assertEquals("Ticket with ID 1 is already canceled.", exception.getMessage());
        
        // Verify interactions
        verify(ticketRepository).findById(1);
        verify(eventClient, never()).increaseTicketCount(anyInt());
        verify(ticketRepository, never()).save(any(Ticket.class));
        
        logger.info("Already cancelled ticket test completed successfully");
    }
    
    // Add more tests for other methods following the same pattern...
}