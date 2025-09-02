package com.example.demo.mapper;

import com.example.demo.pojo.HealthRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HealthRecordMapper {
    
    /**
     * Insert a health record
     * @param r Health record object
     * @return Number of affected rows
     */
    int insert(HealthRecord r);
    
    /**
     * Update sharing information
     * @param id Record ID
     * @param shared Whether shared
     * @param sharedWithUserId Shared-to user ID
     * @param sharedWithRole Shared-to role
     * @param sharedAt Shared timestamp
     * @return Number of affected rows
     */
    int updateShareInfo(@Param("id") Long id, 
                       @Param("shared") Boolean shared, 
                       @Param("sharedWithUserId") Long sharedWithUserId, 
                       @Param("sharedWithRole") String sharedWithRole, 
                       @Param("sharedAt") LocalDateTime sharedAt);
    
    /**
     * Query health records by user ID and time range
     * @param userId User ID
     * @param startIso Start time (ISO format)
     * @param endIso End time (ISO format)
     * @return List of health records
     */
    List<HealthRecord> listByUserAndRange(@Param("userId") Long userId, 
                                         @Param("startIso") String startIso, 
                                         @Param("endIso") String endIso);
    
    /**
     * Query health records by user ID, type and time range
     * @param userId User ID
     * @param type Record type
     * @param startIso Start time (ISO format)
     * @param endIso End time (ISO format)
     * @return List of health records
     */
    List<HealthRecord> listByUserAndType(@Param("userId") Long userId, 
                                        @Param("type") String type, 
                                        @Param("startIso") String startIso, 
                                        @Param("endIso") String endIso);
    
    /**
     * Get the user's latest health record
     * @param userId User ID
     * @return Latest health record
     */
    HealthRecord latestByUser(@Param("userId") Long userId);
    
    /**
     * Delete a health record by ID
     * @param id Record ID
     * @return Number of affected rows
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * Get all health records
     * @return List of all health records
     */
    @Select("SELECT * FROM health_records")
    List<HealthRecord> findAll();
    
    /**
     * Get all health records (XML mapping)
     * @return List of all health records
     */
    List<HealthRecord> listAll();
    
    /**
     * Get total record count
     * @return Total count
     */
    @Select("SELECT COUNT(*) FROM health_records")
    long count();
}

