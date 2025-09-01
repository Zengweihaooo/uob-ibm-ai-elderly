package com.example.demo.service.memoir;

import com.example.demo.mapper.MemoirMapper;
import com.example.demo.pojo.memoir.MemoirProject;
import com.example.demo.pojo.memoir.MemoirSegment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Memoir service layer
 * Provides basic business logic and input validation
 */
@Service
public class MemoirService {
    private final MemoirMapper mapper;

    public MemoirService(MemoirMapper mapper) {
        this.mapper = mapper;
    }

    /** Create project */
    public MemoirProject createProject(String title, String owner, String locale, String pinHash) {
        if (!StringUtils.hasText(title)) throw new IllegalArgumentException("title must not be empty");
        MemoirProject p = new MemoirProject();
        p.setTitle(title.trim());
        p.setOwner(StringUtils.hasText(owner) ? owner.trim() : null);
        p.setLocale(StringUtils.hasText(locale) ? locale : "en-US");
        p.setPinHash(StringUtils.hasText(pinHash) ? pinHash : null);
        mapper.insertProject(p);
        return mapper.findProjectById(p.getId());
    }

    /** List projects by owner */
    public List<MemoirProject> listProjects(String owner) {
        return mapper.listProjectsByOwner(owner);
    }

    /** Add segment */
    public MemoirSegment addSegment(MemoirSegment seg) {
        if (seg.getProjectId() == null) throw new IllegalArgumentException("projectId is required");
        if (!StringUtils.hasText(seg.getChapter())) throw new IllegalArgumentException("chapter is required");
        mapper.insertSegment(seg);
        return seg;
    }

    /** List segments by project */
    public List<MemoirSegment> listSegments(Integer projectId) {
        return mapper.listSegmentsByProject(projectId);
    }
}
