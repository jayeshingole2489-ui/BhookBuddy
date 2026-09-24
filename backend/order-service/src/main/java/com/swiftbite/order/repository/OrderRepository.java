package com.swiftbite.order.repository;

import com.swiftbite.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(String orderId);

    Optional<Order> findByOrderIdAndUserId(String orderId, Long userId);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
}
