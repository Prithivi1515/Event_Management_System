package com.example.demo;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.when;
//
//import java.util.Optional;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import com.example.demo.dto.Property;
//import com.example.demo.exception.PaymentIdNotFoundException;
//import com.example.demo.feignclient.PropertyClient;
//import com.example.demo.model.Payment;
//import com.example.demo.repository.PaymentRepository;
//import com.example.demo.service.PaymentServiceImpl;
//
class PaymentApplicationTests {
//
//    @Mock
//    private PaymentRepository paymentRepository;
//
//    @Mock
//    private PropertyClient propertyClient;
//
////    @InjectMocks
////    private PaymentServiceImpl paymentService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testProcessPaymentThrowsIllegalStateExceptionWhenPropertySoldOut() {
//        Payment payment = new Payment();
//        payment.setPropertyId(101);
//
//        Property property = new Property();
//        property.setPropertyId(101);
//        property.setStatus("SoldOut");
//
//        when(propertyClient.getProperty(101)).thenReturn(property);
//
//        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
//            paymentService.processPayment(payment);
//        });
//
//        assertEquals("Payment failed: Property is already sold out.", exception.getMessage());
//    }
//
//    @Test
//    void testViewPaymentByIdThrowsPaymentIdNotFoundException() {
//        int invalidPaymentId = 999;
//
//        when(paymentRepository.findById(invalidPaymentId)).thenReturn(Optional.empty());
//
//        assertThrows(PaymentIdNotFoundException.class, () -> {
//            paymentService.viewPaymentById(invalidPaymentId);
//        });
//    }
}
