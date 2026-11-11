package com.smartservice.smartservice.controller;

import com.smartservice.smartservice.entity.Provider;
import com.smartservice.smartservice.repository.ProviderRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@RestController
@RequestMapping("/api/providers")
public class ProviderController {

    private final ProviderRepository providerRepository;

    public ProviderController(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    @GetMapping
    public List<Provider> getAllProviders() {
        return providerRepository.findAll();
    }
    @GetMapping("/{id}")
    public Provider getProviderById(@PathVariable("id") Long id) {
        return providerRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
        HttpStatus.NOT_FOUND,
                                "Provider not found with ID: " + id
                        ));
    }
@PostMapping
public Provider createProvider(@RequestBody Provider provider) {

    if (provider.getName() == null ||
            provider.getName().trim().isEmpty()) {
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Provider name is required"
        );
    }

    if (provider.getPhone() == null ||
            provider.getPhone().trim().isEmpty()) {
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Provider phone number is required"
        );
    }

    if (provider.getLocation() == null ||
            provider.getLocation().trim().isEmpty()) {
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Provider location is required"
        );
    }

    if (provider.getExperience() == null ||
            provider.getExperience().trim().isEmpty()) {
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Provider experience is required"
        );
    }

    try {
    int experience = Integer.parseInt(provider.getExperience().trim());

    if (experience < 0) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Experience cannot be negative"
        );
    }

} catch (NumberFormatException e) {
    throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Experience must be a valid whole number"
    );
}

return providerRepository.save(provider);
}

@DeleteMapping("/{id}")
public void deleteProvider(@PathVariable("id") Long id) {
    if (!providerRepository.existsById(id)) {
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND,
                "Provider not found with ID: " + id
        );
    }

    providerRepository.deleteById(id);
}

}