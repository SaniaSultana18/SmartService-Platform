package com.smartservice.smartservice.dto;

import java.time.LocalDateTime;
import com.smartservice.smartservice.dto.BookingRequest;
public class BookingRequest {

    private Long serviceId;
    private Long providerId;
    private LocalDateTime bookingDateTime;

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public LocalDateTime getBookingDateTime() {
        return bookingDateTime;
    }

    public void setBookingDateTime(LocalDateTime bookingDateTime) {
        this.bookingDateTime = bookingDateTime;
    }
}