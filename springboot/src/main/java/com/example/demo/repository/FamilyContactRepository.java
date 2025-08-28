package com.example.demo.repository;

import com.example.demo.pojo.FamilyContact;
import java.util.List;
import java.util.Optional;

public interface FamilyContactRepository {
    
    Optional<FamilyContact> findById(Long id);
    
    FamilyContact save(FamilyContact familyContact);
    
    FamilyContact update(FamilyContact familyContact);
    
    void deleteById(Long id);
    
    List<FamilyContact> findAll();
    
    long count();
}
