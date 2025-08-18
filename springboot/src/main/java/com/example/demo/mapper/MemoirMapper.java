package com.example.demo.mapper;

import com.example.demo.pojo.memoir.MemoirProject;
import com.example.demo.pojo.memoir.MemoirSegment;
import com.example.demo.pojo.memoir.MemoirShareToken;
import com.example.demo.pojo.memoir.MemoirShareGuard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 回忆录模块 MyBatis Mapper
 * 中文注释：提供回忆录项目与分段的基本CRUD
 */
@Mapper
public interface MemoirMapper {
    // ==== Project ====
    int insertProject(MemoirProject project);
    MemoirProject findProjectById(@Param("id") Integer id);
    List<MemoirProject> listProjectsByOwner(@Param("owner") String owner);

    // ==== Segment ====
    int insertSegment(MemoirSegment segment);
    List<MemoirSegment> listSegmentsByProject(@Param("projectId") Integer projectId);

    // ==== Share ====
    int insertShareToken(MemoirShareToken share);
    MemoirShareToken findShareByToken(@Param("token") String token);
    int insertShareGuard(MemoirShareGuard guard);
    MemoirShareGuard findShareGuard(@Param("shareId") Integer shareId);
    int incrementDownloadCount(@Param("shareId") Integer shareId);
}
