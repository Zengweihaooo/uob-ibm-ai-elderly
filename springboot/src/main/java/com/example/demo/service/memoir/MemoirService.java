package com.example.demo.service.memoir;

import com.example.demo.mapper.MemoirMapper;
import com.example.demo.pojo.memoir.MemoirProject;
import com.example.demo.pojo.memoir.MemoirSegment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 回忆录服务层
 * 中文注释：封装基础业务逻辑与入参校验
 */
@Service
public class MemoirService {
    private final MemoirMapper mapper;

    public MemoirService(MemoirMapper mapper) {
        this.mapper = mapper;
    }

    /** 新建项目 */
    public MemoirProject createProject(String title, String owner, String locale, String pinHash) {
        if (!StringUtils.hasText(title)) throw new IllegalArgumentException("title 不能为空");
        MemoirProject p = new MemoirProject();
        p.setTitle(title.trim());
        p.setOwner(StringUtils.hasText(owner) ? owner.trim() : null);
        p.setLocale(StringUtils.hasText(locale) ? locale : "en-US");
        p.setPinHash(StringUtils.hasText(pinHash) ? pinHash : null);
        mapper.insertProject(p);
        return mapper.findProjectById(p.getId());
    }

    /** 按拥有者列出项目 */
    public List<MemoirProject> listProjects(String owner) {
        return mapper.listProjectsByOwner(owner);
    }

    /** 添加分段 */
    public MemoirSegment addSegment(MemoirSegment seg) {
        if (seg.getProjectId() == null) throw new IllegalArgumentException("projectId 必填");
        if (!StringUtils.hasText(seg.getChapter())) throw new IllegalArgumentException("chapter 必填");
        mapper.insertSegment(seg);
        return seg;
    }

    /** 按项目列出分段 */
    public List<MemoirSegment> listSegments(Integer projectId) {
        return mapper.listSegmentsByProject(projectId);
    }
}
