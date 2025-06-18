package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
//	List<Payment> findByBuyerId(Integer buyerId);
//	Payment findByRazorpayOrderId(String orderId);
}
