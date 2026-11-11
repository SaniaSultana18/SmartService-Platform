package com.smartservice.smartservice.repository;

import com.smartservice.smartservice.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByProvider_IdAndBookingDateTimeAndStatusIn(
            Long providerId,
            LocalDateTime bookingDateTime,
            List<String> statuses
    );

    List<Booking> findByCustomer_Id(Long customerId);
}