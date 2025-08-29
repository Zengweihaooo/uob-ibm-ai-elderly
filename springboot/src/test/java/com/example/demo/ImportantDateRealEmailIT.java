package com.example.demo;

import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.ImportantDate;
import com.example.demo.pojo.User;
import com.example.demo.service.ImportantDateService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

/**
 * REAL email integration test (will attempt to send an actual email).
 * Precondition: spring.mail.* is correctly configured and the SMTP account allows sending.
 */
@SpringBootTest
public class ImportantDateRealEmailIT {

    @Autowired
    private ImportantDateService importantDateService;

    @Autowired
    private UserMapper userMapper;

    private static final String TARGET_EMAIL = "1534435440@qq.com";
    private Long testUserId;
    private Long createdDateId;

    @BeforeEach
    void ensureUserExists() {
        User existing = userMapper.findByEmail(TARGET_EMAIL);
        if (existing == null) {
            User u = new User(TARGET_EMAIL);
            // Satisfy NOT NULL constraints
            u.setPasswordHash("test_password");
            userMapper.insert(u);
            existing = userMapper.findByEmail(TARGET_EMAIL);
            Assertions.assertNotNull(existing, "Failed to insert test user");
        }
        testUserId = existing.getId();
        Assertions.assertNotNull(testUserId, "Test user id should not be null");
    }

    @AfterEach
    void cleanup() {
        try {
            if (createdDateId != null) {
                importantDateService.deleteImportantDate(createdDateId);
            }
        } catch (Exception ignore) {}
        createdDateId = null;
    }

    @Test
    void sendWeekReminder_realEmail_shouldMarkAsSent() {
        // Arrange: today + 7 days, enabled=true
        LocalDate target = LocalDate.now().plusDays(7);
        String title = "REAL-EMAIL-IT-" + System.currentTimeMillis();

        ImportantDate created = importantDateService.addImportantDate(
                testUserId,
                title,
                target,
                "custom",
                "real email integration test"
        );
        // Persist enabled + repeatCycle
        ImportantDate toUpdate = new ImportantDate();
        toUpdate.setId(created.getId());
        toUpdate.setTitle(created.getTitle());
        toUpdate.setDescription(created.getDescription());
        toUpdate.setDate(created.getDate());
        toUpdate.setType(created.getType());
        toUpdate.setRepeatCycle("yearly");
        toUpdate.setEnabled(true);
        ImportantDate updated = importantDateService.updateImportantDate(created.getId(), toUpdate);
        createdDateId = updated.getId();

        // Act: trigger sending once
        importantDateService.sendAllPendingReminders();

        // Assert: reload and verify week_reminder_sent is set (means the service considered it sent)
        List<ImportantDate> dates = importantDateService.getImportantDatesByUser(testUserId);
        ImportantDate persisted = dates.stream()
                .filter(d -> d.getId().equals(createdDateId))
                .findFirst()
                .orElse(null);
        Assertions.assertNotNull(persisted, "Created important date should exist");
        Assertions.assertNotNull(persisted.getWeekReminderSent(),
                "week_reminder_sent should be set after sending");
    }
}


