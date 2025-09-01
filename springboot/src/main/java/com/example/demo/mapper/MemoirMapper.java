package com.example.demo.mapper;

import com.example.demo.pojo.memoir.MemoirProject;
import com.example.demo.pojo.memoir.MemoirSegment;
import com.example.demo.pojo.memoir.MemoirShareToken;
import com.example.demo.pojo.memoir.MemoirShareGuard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Memoir module MyBatis Mapper.
 * Provides basic CRUD for memoir projects, segments, and sharing.
 */
@Mapper
public interface MemoirMapper {
    // ==== Project ====
    int insertProject(MemoirProject project);
    MemoirProject findProjectById(@Param("id") Integer id);
    List<MemoirProject> listProjectsByOwner(@Param("owner") String owner);
    MemoirProject findProjectByTitleAndOwner(@Param("title") String title, @Param("owner") String owner);

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
