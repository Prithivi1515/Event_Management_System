package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.demo.dto.Event;
import com.example.demo.dto.NotificationRequest;
import com.example.demo.dto.User;
import com.example.demo.exception.EventNotFoundException;
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
    private TicketRepository repository; // Renamed to match implementation field name

    @Mock
    private EventClient eventClient;
    
    @Mock
    private UserClient userClient;
    
    @Mock
    private NotificationClient notificationClient;
    
    // No @InjectMocks - we'll create the service manually to match constructor order
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
        // Initialize mocks manually
        closeable = MockitoAnnotations.openMocks(this);
        
        // Create the service with constructor order matching @AllArgsConstructor
        ticketService = new TicketServiceImpl(repository, eventClient, userClient, notificationClient);
        
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
        reset(repository, userClient, eventClient, notificationClient);
        
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
        when(repository.existsByUserIdAndEventId(anyInt(), anyInt())).thenReturn(false);
        when(repository.save(any(Ticket.class))).thenReturn(ticket);
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
        verify(repository).existsByUserIdAndEventId(10, 20);
        verify(repository).save(any(Ticket.class));
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
        
        assertEquals(String.format("User not found with ID: %d", ticket.getUserId()), exception.getMessage());
        
        // Verify interactions
        verify(userClient).getUserById(10);
        verify(eventClient, never()).getEventById(anyInt());
        verify(repository, never()).save(any(Ticket.class));
        
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
        
        assertEquals(String.format("Event not found with ID: %d", ticket.getEventId()), exception.getMessage());
        
        // Verify interactions
        verify(userClient).getUserById(10);
        verify(eventClient).getEventById(20);
        verify(repository, never()).save(any(Ticket.class));
        
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
        
        assertEquals(String.format("No tickets available for event ID: %d", ticket.getEventId()), 
                exception.getMessage());
        
        // Verify interactions
        verify(userClient).getUserById(10);
        verify(eventClient).getEventById(20);
        verify(repository, never()).save(any(Ticket.class));
        
        logger.info("No tickets available test completed successfully");
    }
    
    @Test
    @DisplayName("Cancel Ticket - Success")
    void testCancelTicket_Success() {
        // Arrange
        logger.info("Testing cancel ticket");
        
        when(repository.findById(anyInt())).thenReturn(Optional.of(ticket));
        when(repository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            t.setStatus(Status.CANCELLED); // Ensure status is set correctly
            return t;
        });
        doNothing().when(eventClient).increaseTicketCount(anyInt());
        doNothing().when(notificationClient).sendNotification(any(NotificationRequest.class));
        
        // Act
        Ticket canceled = ticketService.cancelTicket(1);
        
        // Assert
        assertNotNull(canceled);
        assertEquals(Status.CANCELLED, canceled.getStatus());
        
        // Verify interactions
        verify(repository).findById(1);
        verify(eventClient).increaseTicketCount(20);
        verify(repository).save(any(Ticket.class));
        verify(notificationClient).sendNotification(any(NotificationRequest.class));
        
        logger.info("Cancel ticket test completed successfully");
    }
    
    @Test
    @DisplayName("Cancel Ticket - Already Cancelled")
    void testCancelTicket_AlreadyCancelled() {
        // Arrange
        logger.info("Testing cancellation of already cancelled ticket");
        
        ticket.setStatus(Status.CANCELLED); // Set ticket as already cancelled
        
        when(repository.findById(anyInt())).thenReturn(Optional.of(ticket));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> ticketService.cancelTicket(1));
        
        assertEquals(String.format("Ticket with ID %d is already canceled", 1), 
                exception.getMessage());
        
        // Verify interactions
        verify(repository).findById(1);
        verify(eventClient, never()).increaseTicketCount(anyInt());
        verify(repository, never()).save(any(Ticket.class));
        
        logger.info("Already cancelled ticket test completed successfully");
    }
    
    @Test
    @DisplayName("Get Tickets By User ID")
    void testGetTicketsByUserId() {
        // Arrange
        List<Ticket> expectedTickets = Collections.singletonList(ticket);
        when(userClient.getUserById(anyInt())).thenReturn(user);
        when(repository.findByUserId(anyInt())).thenReturn(expectedTickets);
        
        // Act
        List<Ticket> result = ticketService.getTicketsByUserId(10);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ticket, result.get(0));
        
        // Verify
        verify(userClient).getUserById(10);
        verify(repository).findByUserId(10);
    }
    
    @Test
    @DisplayName("Get Tickets By Status")
    void testGetTicketsByStatus() {
        // Arrange
        List<Ticket> expectedTickets = Arrays.asList(ticket);
        when(repository.findByStatus(any(Status.class))).thenReturn(expectedTickets);
        
        // Act
        List<Ticket> result = ticketService.getTicketsByStatus(Status.BOOKED);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Status.BOOKED, result.get(0).getStatus());
        
        // Verify
        verify(repository).findByStatus(Status.BOOKED);
    }
}