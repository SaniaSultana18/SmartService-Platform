package com.smartservice.smartservice.repository;

import com.smartservice.smartservice.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderRepository extends JpaRepository<Provider, Long> {
}