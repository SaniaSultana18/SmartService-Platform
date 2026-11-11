
package com.smartservice.smartservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "customer_id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
private User customer;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "service_id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
private Service service;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "provider_id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
private Provider provider;

    private LocalDateTime bookingDateTime;

    private String status;

    public Booking() {
    }

    public Booking(
            User customer,
            Service service,
            Provider provider,
            LocalDateTime bookingDateTime,
            String status) {

        this.customer = customer;
        this.service = service;
        this.provider = provider;
        this.bookingDateTime = bookingDateTime;
        this.status = status;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
public Long getId() {
    return id;
}

    public void setId(Long id) {
        this.id = id;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public LocalDateTime getBookingDateTime() {
        return bookingDateTime;
    }

    public void setBookingDateTime(LocalDateTime bookingDateTime) {
        this.bookingDateTime = bookingDateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
