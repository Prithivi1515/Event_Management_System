package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceApplicationTests {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceApplicationTests.class);
    
    @Mock
    private UserRepository repository;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    private AutoCloseable closeable;
    private User testUser;
    
    @BeforeAll
    void setUpTestSuite() {
        logger.info("Starting UserService test suite");
    }
    
    @BeforeEach
    void setUp() {
        // Initialize mocks manually to get access to the AutoCloseable
        closeable = MockitoAnnotations.openMocks(this);
        
        // Create test user
        testUser = new User();
        testUser.setUserId(1);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("password");
        testUser.setContactNumber("1234567890");
        testUser.setRoles("USER");
        
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
        logger.info("Completed UserService test suite");
    }

    @Test
    @DisplayName("Save User - Success")
    public void testSaveUser() {
        // Arrange
        when(repository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(repository.save(any(User.class))).thenReturn(testUser);
        
        // Act
        String result = userService.saveUser(testUser);
        
        // Assert
        assertEquals("User saved successfully!", result);
        verify(repository).existsByEmailIgnoreCase(testUser.getEmail());
        verify(repository).save(testUser);
    }
    
    @Test
    @DisplayName("Save User - Email Already Exists")
    public void testSaveUser_EmailExists() {
        // Arrange
        when(repository.existsByEmailIgnoreCase(anyString())).thenReturn(true);
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.saveUser(testUser);
        });
        
        assertEquals("Email already exists: " + testUser.getEmail(), exception.getMessage());
        verify(repository).existsByEmailIgnoreCase(testUser.getEmail());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Update User - Success")
    public void testUpdateUser() {
        // Arrange
        User existingUser = new User();
        existingUser.setUserId(1);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");
        
        when(repository.findById(anyInt())).thenReturn(Optional.of(existingUser));
        when(repository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenReturn(existingUser);
        
        // Act
        String result = userService.updateUser(1, testUser);
        
        // Assert
        assertEquals("User updated successfully!", result);
        verify(repository).findById(1);
        verify(repository).findByEmailIgnoreCase(testUser.getEmail());
        verify(repository).save(any(User.class));
    }
    
    @Test
    @DisplayName("Update User - Not Found")
    public void testUpdateUser_NotFound() {
        // Arrange
        when(repository.findById(anyInt())).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(1, testUser);
        });
        
        assertEquals("User not found with ID: 1", exception.getMessage());
        verify(repository).findById(1);
        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Get User By ID - Success")
    public void testGetUserById() {
        // Arrange
        when(repository.findById(anyInt())).thenReturn(Optional.of(testUser));
        
        // Act
        User result = userService.getUser(1);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(repository).findById(1);
    }
    
    @Test
    @DisplayName("Get User By ID - Not Found")
    public void testGetUserById_UserNotFound() {
        // Arrange
        when(repository.findById(anyInt())).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUser(100);
        });
        
        assertEquals("User not found with ID: 100", exception.getMessage());
        verify(repository).findById(100);
    }

    @Test
    @DisplayName("Get All Users - Success")
    public void testGetAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(repository.findAll()).thenReturn(users);
        
        // Act
        List<User> result = userService.getAllUsers();
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
        verify(repository).findAll();
    }
    
    @Test
    @DisplayName("Get All Users - Empty")
    public void testGetAllUsers_Empty() {
        // Arrange
        when(repository.findAll()).thenReturn(new ArrayList<>());
        
        // Act & Assert
        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getAllUsers();
        });
        
        assertEquals("No users found in the system.", exception.getMessage());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Delete User - Success")
    public void testDeleteUserById() {
        // Arrange
        when(repository.findById(anyInt())).thenReturn(Optional.of(testUser));
        doNothing().when(repository).delete(any(User.class));
        
        // Act
        String result = userService.deleteUser(1);
        
        // Assert
        assertEquals("User deleted successfully!", result);
        verify(repository).findById(1);
        verify(repository).delete(testUser);
    }
    
    @Test
    @DisplayName("Delete User - Not Found")
    public void testDeleteUserById_NotFound() {
        // Arrange
        when(repository.findById(anyInt())).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(1);
        });
        
        assertEquals("Cannot delete, user not found with ID: 1", exception.getMessage());
        verify(repository).findById(1);
        verify(repository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("Get User By Email - Success")
    public void testGetUserByEmail() {
        // Arrange
        when(repository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        
        // Act
        User result = userService.getUserByEmail("john@example.com");
        
        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(repository).findByEmailIgnoreCase("john@example.com");
    }
    
    @Test
    @DisplayName("Get User By Email - Not Found")
    public void testGetUserByEmail_NotFound() {
        // Arrange
        when(repository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserByEmail("nonexistent@example.com");
        });
        
        assertEquals("User not found with email: nonexistent@example.com", exception.getMessage());
        verify(repository).findByEmailIgnoreCase("nonexistent@example.com");
    }

    @Test
    @DisplayName("Get Users By Role - Success")
    public void testGetUsersByRole() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(repository.findByRoles(anyString())).thenReturn(users);
        
        // Act
        List<User> result = userService.getUsersByRole("USER");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("USER", result.get(0).getRoles());
        verify(repository).findByRoles("USER");
    }
    
    @Test
    @DisplayName("Get Users By Role - Empty")
    public void testGetUsersByRole_Empty() {
        // Arrange
        when(repository.findByRoles(anyString())).thenReturn(new ArrayList<>());
        
        // Act & Assert
        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUsersByRole("ADMIN");
        });
        
        assertEquals("No users found with role: ADMIN", exception.getMessage());
        verify(repository).findByRoles("ADMIN");
    }

    @Test
    @DisplayName("Search Users By Name - Success")
    public void testSearchUsersByName() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(repository.findByNameContainingIgnoreCase(anyString())).thenReturn(users);
        
        // Act
        List<User> result = userService.searchUsersByName("John");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getName().contains("John"));
        verify(repository).findByNameContainingIgnoreCase("John");
    }
    
    @Test
    @DisplayName("Search Users By Name - Empty")
    public void testSearchUsersByName_Empty() {
        // Arrange
        when(repository.findByNameContainingIgnoreCase(anyString())).thenReturn(new ArrayList<>());
        
        // Act & Assert
        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.searchUsersByName("Unknown");
        });
        
        assertEquals("No users found matching name: Unknown", exception.getMessage());
        verify(repository).findByNameContainingIgnoreCase("Unknown");
    }
}
