package com.example.demo.repository;

import com.example.demo.pojo.ImportantDate;
import java.util.List;
import java.util.Optional;

public interface ImportantDateRepository {
    
    Optional<ImportantDate> findById(Long id);
    
    ImportantDate save(ImportantDate importantDate);
    
    ImportantDate update(ImportantDate importantDate);
    
    void deleteById(Long id);
    
    List<ImportantDate> findAll();
    
    long count();
}
