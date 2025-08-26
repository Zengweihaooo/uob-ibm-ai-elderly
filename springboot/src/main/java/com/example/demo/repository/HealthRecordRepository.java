package com.example.demo.repository;

import com.example.demo.pojo.HealthRecord;
import java.util.List;
import java.util.Optional;

public interface HealthRecordRepository {
    
    Optional<HealthRecord> findById(Long id);
    
    HealthRecord save(HealthRecord healthRecord);
    
    HealthRecord update(HealthRecord healthRecord);
    
    void deleteById(Long id);
    
    List<HealthRecord> findAll();
    
    long count();
}
