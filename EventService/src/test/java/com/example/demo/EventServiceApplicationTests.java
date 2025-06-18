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

import com.example.demo.dto.User;
import com.example.demo.exception.EventNotFoundException;
import com.example.demo.feignclient.UserClient;
import com.example.demo.model.Event;
import com.example.demo.repository.EventRepository;
import com.example.demo.service.EventServiceImpl;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventServiceApplicationTests {
    
    private static final Logger logger = LoggerFactory.getLogger(EventServiceApplicationTests.class);
    
    @Mock
    private EventRepository repository;
    
    @Mock
    private UserClient userClient;
    
    private EventServiceImpl service;
    
    private AutoCloseable closeable;
    private Event testEvent;
    private User testUser;
    
    @BeforeAll
    void setUpTestSuite() {
        logger.info("Starting EventService test suite");
    }
    
    @BeforeEach
    void setup() {
        // Initialize mocks properly
        closeable = MockitoAnnotations.openMocks(this);
        
        // Manually create the service instance with constructor parameters
        service = new EventServiceImpl(repository, userClient);
        
        // Create test event
        testEvent = new Event();
        testEvent.setEventId(1);
        testEvent.setName("Sample Event");
        testEvent.setCategory("Tech");
        testEvent.setLocation("New York");
        testEvent.setDate(LocalDateTime.now().plusDays(7));
        testEvent.setTicketCount(10);
        testEvent.setOrganizerId(101);
        
        // Create test user
        testUser = new User();
        testUser.setUserId(101);
        testUser.setName("Test Organizer");
        testUser.setRoles("organizer");
        
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
        logger.info("Completed EventService test suite");
    }

    @Test
    @DisplayName("Create Event - Success")
    void testCreateEvent_Success() {
        // Arrange
        when(userClient.getUserById(101)).thenReturn(testUser);
        when(repository.save(any(Event.class))).thenReturn(testEvent);
        
        // Act
        String result = service.createEvent(testEvent);
        
        // Assert
        assertEquals("Event created successfully.", result);
        
        // Verify
        verify(userClient).getUserById(101);
        verify(repository).save(testEvent);
    }
    
    @Test
    @DisplayName("Create Event - Not An Organizer")
    void testCreateEvent_NotAnOrganizer() {
        // Arrange
        testUser.setRoles("user"); // Change role to non-organizer
        when(userClient.getUserById(101)).thenReturn(testUser);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> service.createEvent(testEvent));
        
        assertEquals("User is not an organizer or does not exist.", exception.getMessage());
        
        // Verify
        verify(userClient).getUserById(101);
        verify(repository, never()).save(any(Event.class));
    }
    
    @Test
    @DisplayName("Get Event By ID - Success")
    void testGetEventById_Success() {
        // Arrange
        when(repository.findById(1)).thenReturn(Optional.of(testEvent));
        
        // Act
        Event result = service.getEventById(1);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getEventId());
        assertEquals("Sample Event", result.getName());
        
        // Verify
        verify(repository).findById(1);
    }
    
    @Test
    @DisplayName("Get Event By ID - Not Found")
    void testGetEventById_NotFound() {
        // Arrange
        when(repository.findById(999)).thenReturn(Optional.empty());
        
        // Act & Assert
        EventNotFoundException exception = assertThrows(EventNotFoundException.class, 
                () -> service.getEventById(999));
        
        assertEquals("Event not found with ID: 999", exception.getMessage());
        
        // Verify
        verify(repository).findById(999);
    }
    
    @Test
    @DisplayName("Get All Events - Success")
    void testGetAllEvents_Success() {
        // Arrange
        List<Event> events = Arrays.asList(testEvent);
        when(repository.findAll()).thenReturn(events);
        
        // Act
        List<Event> result = service.getAllEvents();
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sample Event", result.get(0).getName());
        
        // Verify
        verify(repository).findAll();
    }
    
    @Test
    @DisplayName("Get All Events - Empty")
    void testGetAllEvents_Empty() {
        // Arrange
        when(repository.findAll()).thenReturn(new ArrayList<>());
        
        // Act & Assert
        EventNotFoundException exception = assertThrows(EventNotFoundException.class, 
                () -> service.getAllEvents());
        
        assertEquals("No events found in the system.", exception.getMessage());
        
        // Verify
        verify(repository).findAll();
    }
    
    @Test
    @DisplayName("Update Event - Success")
    void testUpdateEvent_Success() {
        // Arrange
        when(repository.existsById(1)).thenReturn(true);
        when(repository.save(any(Event.class))).thenReturn(testEvent);
        
        // Create updated event
        Event updatedEvent = new Event();
        updatedEvent.setName("Updated Event");
        
        // Act
        String result = service.updateEvent(1, updatedEvent);
        
        // Assert
        assertEquals("Event updated successfully.", result);
        
        // Verify ID was set correctly before saving
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(repository).save(eventCaptor.capture());
        Event capturedEvent = eventCaptor.getValue();
        assertEquals(1, capturedEvent.getEventId());
        assertEquals("Updated Event", capturedEvent.getName());
    }
    
    @Test
    @DisplayName("Update Event - Not Found")
    void testUpdateEvent_NotFound() {
        // Arrange
        when(repository.existsById(999)).thenReturn(false);
        
        // Create updated event
        Event updatedEvent = new Event();
        updatedEvent.setName("Updated Event");
        
        // Act & Assert
        EventNotFoundException exception = assertThrows(EventNotFoundException.class, 
                () -> service.updateEvent(999, updatedEvent));
        
        assertEquals("Cannot update: Event not found with ID: 999", exception.getMessage());
        
        // Verify
        verify(repository).existsById(999);
        verify(repository, never()).save(any(Event.class));
    }
    
    @Test
    @DisplayName("Delete Event - Success")
    void testDeleteEvent_Success() {
        // Arrange
        when(repository.findById(1)).thenReturn(Optional.of(testEvent));
        doNothing().when(repository).delete(any(Event.class));
        
        // Act
        String result = service.deleteEvent(1);
        
        // Assert
        assertEquals("Event deleted successfully.", result);
        
        // Verify
        verify(repository).findById(1);
        verify(repository).delete(testEvent);
    }
    
    @Test
    @DisplayName("Delete Event - Not Found")
    void testDeleteEvent_NotFound() {
        // Arrange
        when(repository.findById(999)).thenReturn(Optional.empty());
        
        // Act & Assert
        EventNotFoundException exception = assertThrows(EventNotFoundException.class, 
                () -> service.deleteEvent(999));
        
        assertEquals("Event not found with ID: 999", exception.getMessage());
        
        // Verify
        verify(repository).findById(999);
        verify(repository, never()).delete(any(Event.class));
    }
    
//    @Test
//    @DisplayName("Decrease Ticket Count - Success")
//    void testDecreaseTicketCount_Success() {
//        // Arrange
//        when(repository.findById(1)).thenReturn(Optional.of(testEvent));
//        when(repository.save(any(Event.class))).thenAnswer(invocation -> {
//            Event savedEvent = invocation.getArgument(0);
//            return savedEvent;
//        });
//        
//        // Act
//        service.decreaseTicketCount(1);
//        
//        // Assert
//        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
//        verify(repository).save(eventCaptor.capture());
//        Event savedEvent = eventCaptor.getValue();
//        assertEquals(9, savedEvent.getTicketCount());
//    }
    
//    @Test
//    @DisplayName("Decrease Ticket Count - No Tickets Available")
//    void testDecreaseTicketCount_NoTickets() {
//        // Arrange
//        testEvent.setTicketCount(0);
//        when(repository.findById(1)).thenReturn(Optional.of(testEvent));
//        
//        // Act & Assert
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
//                () -> service.decreaseTicketCount(1));
//        
//        assertEquals("No tickets available for event with ID: 1", exception.getMessage());
//        
//        // Verify
//        verify(repository).findById(1);
//        verify(repository, never()).save(any(Event.class));
//    }
    
//    @Test
//    @DisplayName("Increase Ticket Count - Success")
//    void testIncreaseTicketCount_Success() {
//        // Arrange
//        when(repository.findById(1)).thenReturn(Optional.of(testEvent));
//        when(repository.save(any(Event.class))).thenAnswer(invocation -> {
//            Event savedEvent = invocation.getArgument(0);
//            return savedEvent;
//        });
//        
//        // Act
//        service.increaseTicketCount(1);
//        
//        // Assert
//        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
//        verify(repository).save(eventCaptor.capture());
//        Event savedEvent = eventCaptor.getValue();
//        assertEquals(11, savedEvent.getTicketCount());
//    }
    
    @Test
    @DisplayName("Filter By Category - Success")
    void testFilterByCategory_Success() {
        // Arrange
        List<Event> events = Arrays.asList(testEvent);
        when(repository.findByCategoryIgnoreCase("Tech")).thenReturn(events);
        
        // Act
        List<Event> result = service.filterByCategory("Tech");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sample Event", result.get(0).getName());
        
        // Verify
        verify(repository).findByCategoryIgnoreCase("Tech");
    }
    
    @Test
    @DisplayName("Filter By Category - No Events")
    void testFilterByCategory_NoEvents() {
        // Arrange
        when(repository.findByCategoryIgnoreCase("Sports")).thenReturn(new ArrayList<>());
        
        // Act & Assert
        EventNotFoundException exception = assertThrows(EventNotFoundException.class, 
                () -> service.filterByCategory("Sports"));
        
        assertEquals("No events found for category: Sports", exception.getMessage());
        
        // Verify
        verify(repository).findByCategoryIgnoreCase("Sports");
    }
    
    @Test
    @DisplayName("Filter By Location - Success")
    void testFilterByLocation_Success() {
        // Arrange
        List<Event> events = Arrays.asList(testEvent);
        when(repository.findByLocationIgnoreCase("New York")).thenReturn(events);
        
        // Act
        List<Event> result = service.filterByLocation("New York");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("New York", result.get(0).getLocation());
        
        // Verify
        verify(repository).findByLocationIgnoreCase("New York");
    }
    
    @Test
    @DisplayName("Search Events By Name - Success")
    void testSearchEventsByName_Success() {
        // Arrange
        List<Event> events = Arrays.asList(testEvent);
        when(repository.findByNameContainingIgnoreCase("Sample")).thenReturn(events);
        
        // Act
        List<Event> result = service.searchEventsByName("Sample");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getName().contains("Sample"));
        
        // Verify
        verify(repository).findByNameContainingIgnoreCase("Sample");
    }
    
    @Test
    @DisplayName("Get Events By Organizer - Success")
    void testGetEventsByOrganizer_Success() {
        // Arrange
        List<Event> events = Arrays.asList(testEvent);
        when(repository.findByOrganizerId(101)).thenReturn(events);
        
        // Act
        List<Event> result = service.getEventsByOrganizer(101);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(101, result.get(0).getOrganizerId());
        
        // Verify
        verify(repository).findByOrganizerId(101);
    }
}