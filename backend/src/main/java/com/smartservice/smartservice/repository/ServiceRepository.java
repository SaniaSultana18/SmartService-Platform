package com.smartservice.smartservice.repository;

import com.smartservice.smartservice.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Long> {
}