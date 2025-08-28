package com.example.demo.repository;

import com.example.demo.pojo.Podcast;
import java.util.List;
import java.util.Optional;

public interface PodcastRepository {
    
    Optional<Podcast> findById(Long id);
    
    Podcast save(Podcast podcast);
    
    Podcast update(Podcast podcast);
    
    void deleteById(Long id);
    
    List<Podcast> findAll();
    
    long count();
}
