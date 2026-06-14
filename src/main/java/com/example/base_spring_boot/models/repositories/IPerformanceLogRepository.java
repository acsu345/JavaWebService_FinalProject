package com.example.base_spring_boot.models.repositories;

import com.example.base_spring_boot.models.entities.PerformanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPerformanceLogRepository extends JpaRepository<PerformanceLog, Long> {
}
