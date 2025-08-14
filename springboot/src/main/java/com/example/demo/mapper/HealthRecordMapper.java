package com.example.demo.mapper;

import com.example.demo.pojo.HealthRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HealthRecordMapper {
    
    /**
     * 插入健康记录
     * @param r 健康记录对象
     * @return 影响的行数
     */
    int insert(HealthRecord r);
    
    /**
     * 更新分享信息
     * @param id 记录ID
     * @param shared 是否分享
     * @param sharedWithUserId 分享给的用户ID
     * @param sharedWithRole 分享给的角色
     * @param sharedAt 分享时间
     * @return 影响的行数
     */
    int updateShareInfo(@Param("id") Long id, 
                       @Param("shared") Boolean shared, 
                       @Param("sharedWithUserId") Long sharedWithUserId, 
                       @Param("sharedWithRole") String sharedWithRole, 
                       @Param("sharedAt") LocalDateTime sharedAt);
    
    /**
     * 根据用户ID和时间范围查询健康记录
     * @param userId 用户ID
     * @param startIso 开始时间（ISO格式）
     * @param endIso 结束时间（ISO格式）
     * @return 健康记录列表
     */
    List<HealthRecord> listByUserAndRange(@Param("userId") Long userId, 
                                         @Param("startIso") String startIso, 
                                         @Param("endIso") String endIso);
    
    /**
     * 根据用户ID、类型和时间范围查询健康记录
     * @param userId 用户ID
     * @param type 记录类型
     * @param startIso 开始时间（ISO格式）
     * @param endIso 结束时间（ISO格式）
     * @return 健康记录列表
     */
    List<HealthRecord> listByUserAndType(@Param("userId") Long userId, 
                                        @Param("type") String type, 
                                        @Param("startIso") String startIso, 
                                        @Param("endIso") String endIso);
    
    /**
     * 获取用户最新的健康记录
     * @param userId 用户ID
     * @return 最新的健康记录
     */
    HealthRecord latestByUser(@Param("userId") Long userId);
    
    /**
     * 根据ID删除健康记录
     * @param id 记录ID
     * @return 影响的行数
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 获取所有健康记录
     * @return 所有健康记录列表
     */
    List<HealthRecord> listAll();
}

