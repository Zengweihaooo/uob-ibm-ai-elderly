package com.example.demo.config;

import com.example.demo.mapper.MemoirMapper;
import com.example.demo.pojo.memoir.MemoirProject;
import com.example.demo.pojo.memoir.MemoirSegment;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Memoir seed data initializer.
 * Inserts a sample memoir project with several segments for elderly users to explore.
 * Idempotent: checks by (title, owner) before inserting.
 */
@Component
public class MemoirSeeder {
    private static final Logger log = LoggerFactory.getLogger(MemoirSeeder.class);

    private final MemoirMapper mapper;

    @Value("${app.memoir.seed.enabled:true}")
    private boolean enabled;
    @Value("${app.memoir.seed.owner:demo_user}")
    private String owner;
    @Value("${app.memoir.seed.title:Sample Life Story}")
    private String title;
    @Value("${app.memoir.seed.locale:en-GB}")
    private String locale;

    public MemoirSeeder(MemoirMapper mapper) {
        this.mapper = mapper;
    }

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("Memoir seed disabled");
            return;
        }
        if (!StringUtils.hasText(title)) return;
        try {
            MemoirProject existing = mapper.findProjectByTitleAndOwner(title, owner);
            if (existing != null) {
                log.info("Seed memoir already exists: id={} title={}", existing.getId(), title);
                return;
            }
            // Insert project
            MemoirProject project = new MemoirProject();
            project.setTitle(title);
            project.setOwner(owner);
            project.setLocale(locale);
            mapper.insertProject(project);
            log.info("Inserted seed memoir project id={} title={}", project.getId(), title);

            // Prepare sample segments (orderIndex ascending)
            List<MemoirSegment> segments = Arrays.asList(
                seg(project.getId(), 0, "Childhood", "Seaside Beginnings", "I grew up in a small seaside town on the English coast. Mornings smelled of salt and fresh bread, and we spent long summer evenings chasing the tide. The war had ended, and though money was tight, we shared everything—stories, tools, and steaming tea."),
                seg(project.getId(), 1, "Early Adulthood", "First Job", "My first proper job was at a local post office in the early 1960s. Pay was modest, but the pride of earning my own keep felt enormous. I still remember the click of the stamp press, the careful sorting by hand, and the quiet trust of people sending precious letters."),
                seg(project.getId(), 2, "Family Life", "Gatherings", "Sundays became a ritual—roast potatoes crisping in the oven, grandchildren sprawled on the rug, and gentle debates about football and weather. Those afternoons weren’t grand, but they were golden—threads that stitched our generations together."),
                seg(project.getId(), 3, "Reflections", "Lessons Learned", "If there’s one lesson age has taught me, it’s that small kindnesses outlast grand achievements. A handwritten note, a warm cup passed to cold hands, listening without rushing—these become the anchors others remember you by.")
            );
            for (MemoirSegment s : segments) mapper.insertSegment(s);
            log.info("Inserted {} seed memoir segments", segments.size());
        } catch (Exception e) {
            log.error("Failed inserting memoir seed data", e);
        }
    }

    private static MemoirSegment seg(Integer projectId, int order, String chapter, String theme, String text) {
        MemoirSegment s = new MemoirSegment();
        s.setProjectId(projectId);
        s.setOrderIndex(order);
        s.setChapter(chapter);
        s.setTheme(theme);
        s.setText(text);
        return s;
    }
}
