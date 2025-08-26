package com.example.demo.repository;

import com.example.demo.pojo.Memo;
import java.util.List;
import java.util.Optional;

public interface MemoRepository {
    
    Optional<Memo> findById(Long id);
    
    Memo save(Memo memo);
    
    Memo update(Memo memo);
    
    void deleteById(Long id);
    
    List<Memo> findAll();
    
    long count();
}
