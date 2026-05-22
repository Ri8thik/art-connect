package com.project.artconnect.modules.orders.repository;

import com.project.artconnect.common.enums.OrderStatus;
import com.project.artconnect.modules.orders.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Order> findByArtistId(UUID artistId, Pageable pageable);

    Page<Order> findByArtistIdAndStatus(UUID artistId, OrderStatus status, Pageable pageable);

    Page<Order> findByCustomerIdAndStatus(UUID customerId, OrderStatus status, Pageable pageable);

    long countByArtistIdAndStatus(UUID artistId, OrderStatus status);
}

