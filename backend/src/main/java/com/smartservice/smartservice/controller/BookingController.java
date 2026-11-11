
package com.smartservice.smartservice.controller;

import com.smartservice.smartservice.dto.BookingRequest;
import com.smartservice.smartservice.entity.Booking;
import com.smartservice.smartservice.entity.User;
import com.smartservice.smartservice.repository.BookingRepository;
import com.smartservice.smartservice.repository.UserRepository;
import com.smartservice.smartservice.repository.ServiceRepository;
import com.smartservice.smartservice.repository.ProviderRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ProviderRepository providerRepository;

    public BookingController(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            ServiceRepository serviceRepository,
            ProviderRepository providerRepository) {

        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.providerRepository = providerRepository;
    }

    // Get all bookings for admin or the logged-in customer
    @GetMapping
    public List<Booking> getAllBookings(Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return bookingRepository.findAll();
        }

        return bookingRepository.findByCustomer_Id(user.getId());
    }

    // Get one booking by ID
   @GetMapping("/{id}")
public Booking getBookingById(
        @PathVariable("id") Long id,
        Authentication authentication) {

    User user = getAuthenticatedUser(authentication);

    Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Booking not found with id: " + id
            ));

    // Admin can view any booking
    if ("ADMIN".equalsIgnoreCase(user.getRole())) {
        return booking;
    }

    // Customer can view only their own booking
    verifyBookingOwnership(booking, user);

    return booking;
}
    // Admins can view any customer's bookings.
    // Customers can view only their own bookings.
    @GetMapping("/customer/{customerId}")
    public List<Booking> getBookingsByCustomer(
            @PathVariable("customerId") Long customerId,
            Authentication authentication) {

        User customer = getAuthenticatedUser(authentication);

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return bookingRepository.findByCustomer_Id(customerId);
        }

        if (!customer.getId().equals(customerId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only view your own bookings"
            );
        }

        return bookingRepository.findByCustomer_Id(customer.getId());
    }

    // Update booking status based on user role
    @PutMapping("/{id}/status")
    public Booking updateBookingStatus(
            @PathVariable("id") Long id,
            @RequestBody Booking booking,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        if (booking.getStatus() == null
                || booking.getStatus().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Booking status is required"
            );
        }

        String newStatus = booking.getStatus()
                .trim()
                .toUpperCase(Locale.ROOT);

        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found with id: " + id
                ));

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {

            String currentStatus = existingBooking.getStatus()
                    .trim()
                    .toUpperCase(Locale.ROOT);

            boolean validTransition =
                    (currentStatus.equals("PENDING")
                            && List.of("CONFIRMED", "CANCELLED")
                            .contains(newStatus))
                    ||
                    (currentStatus.equals("CONFIRMED")
                            && List.of("COMPLETED", "CANCELLED")
                            .contains(newStatus));

            if (currentStatus.equals(newStatus)) {
                return existingBooking;
            }

            if (!validTransition) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid booking status transition"
                );
            }

            existingBooking.setStatus(newStatus);

        } else {

            verifyBookingOwnership(existingBooking, user);

            if (!"CANCELLED".equals(newStatus)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Customers can only cancel their own bookings"
                );
            }

            if (!"PENDING".equalsIgnoreCase(
                    existingBooking.getStatus())) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Only pending bookings can be cancelled"
                );
            }

            existingBooking.setStatus("CANCELLED");
        }

        return bookingRepository.save(existingBooking);
    }

    // Create a booking for the authenticated customer
    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @RequestBody BookingRequest request,
            Authentication authentication) {

        User customer = getAuthenticatedUser(authentication);

        // Only customers can create bookings
        boolean isCustomer = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_CUSTOMER"));

        if (!isCustomer) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only customers can create bookings"
            );
        }

        // Validate required booking details
        if (request.getServiceId() == null
                || request.getProviderId() == null
                || request.getBookingDateTime() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Service ID, provider ID, and booking date/time are required"
            );
        }

        // Prevent past bookings
        if (request.getBookingDateTime()
                .isBefore(LocalDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Booking date and time cannot be in the past"
            );
        }

        Long serviceId = request.getServiceId();
        Long providerId = request.getProviderId();

        // Retrieve and validate service
        com.smartservice.smartservice.entity.Service service =
                serviceRepository.findById(serviceId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Service not found with ID: " + serviceId
                        ));

        // Retrieve and validate provider
        com.smartservice.smartservice.entity.Provider provider =
                providerRepository.findById(providerId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Provider not found with ID: " + providerId
                        ));

        // Check provider availability
        if (!provider.isAvailable()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This provider is currently unavailable"
            );
        }

        // Prevent double booking for pending or confirmed bookings
        if (bookingRepository.existsByProvider_IdAndBookingDateTimeAndStatusIn(
                providerId,
                request.getBookingDateTime(),
                List.of("PENDING", "CONFIRMED"))) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This provider already has a booking at this date and time"
            );
        }

        // Create a new booking
        Booking newBooking = new Booking();

        newBooking.setCustomer(customer);
        newBooking.setService(service);
        newBooking.setProvider(provider);
        newBooking.setBookingDateTime(request.getBookingDateTime());
        newBooking.setStatus("PENDING");

        Booking savedBooking = bookingRepository.save(newBooking);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedBooking);
    }

    // Require an authenticated user
    private User getAuthenticatedUser(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in first"
            );
        }

        String email = authentication.getName()
                .trim()
                .toLowerCase(Locale.ROOT);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));
    }

    // Ensure users can access only their own bookings
    private void verifyBookingOwnership(
            Booking booking,
            User customer) {

        if (booking.getCustomer() == null
                || booking.getCustomer().getId() == null
                || !booking.getCustomer().getId().equals(
                        customer.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only access your own bookings"
            );
        }
    }
}
