package com.swiftbite.delivery.repository;

import com.swiftbite.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByDeliveryId(String deliveryId);
    Optional<Delivery> findByOrderId(String orderId);
}
