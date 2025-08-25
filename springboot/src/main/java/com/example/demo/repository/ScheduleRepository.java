package com.example.demo.repository;

import com.example.demo.pojo.Schedule;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository {
    
    Optional<Schedule> findById(Long id);
    
    Schedule save(Schedule schedule);
    
    Schedule update(Schedule schedule);
    
    void deleteById(Long id);
    
    List<Schedule> findAll();
    
    long count();
}
