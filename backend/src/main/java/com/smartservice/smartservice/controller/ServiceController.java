
package com.smartservice.smartservice.controller;

import com.smartservice.smartservice.entity.Service;
import com.smartservice.smartservice.repository.ServiceRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceRepository serviceRepository;

    public ServiceController(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    // Public: view all services
    @GetMapping
    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    // Public: view one service
    @GetMapping("/{id}")
    public Service getServiceById(@PathVariable("id") Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Service not found with ID: " + id
                        ));
    }

    // Admin access is enforced by SecurityConfig
    @PostMapping
    public Service createService(@RequestBody Service service) {

        if (service.getName() == null
                || service.getName().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Service name is required"
            );
        }

        if (service.getDescription() == null
                || service.getDescription().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Service description is required"
            );
        }

        if (service.getCategory() == null
                || service.getCategory().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Service category is required"
            );
        }

        if (service.getPrice() == null ||
                service.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Service price must be greater than zero"
            );
        }

        return serviceRepository.save(service);
    }
}
