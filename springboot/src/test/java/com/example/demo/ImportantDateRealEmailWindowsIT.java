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
import java.util.ArrayList;
import java.util.List;

/**
 * REAL email integration test for both windows (T+7 and T+1).
 * Precondition: spring.mail.* correctly configured; test inbox can receive external emails.
 */
@SpringBootTest
public class ImportantDateRealEmailWindowsIT {

    @Autowired
    private ImportantDateService importantDateService;

    @Autowired
    private UserMapper userMapper;

    private static final String TARGET_EMAIL = "1534435440@qq.com";
    private Long testUserId;
    private final List<Long> createdIds = new ArrayList<>();

    @BeforeEach
    void ensureUserExists() {
        User u = userMapper.findByEmail(TARGET_EMAIL);
        if (u == null) {
            u = new User(TARGET_EMAIL);
            u.setPasswordHash("test_password");
            userMapper.insert(u);
            u = userMapper.findByEmail(TARGET_EMAIL);
        }
        Assertions.assertNotNull(u, "Test user should exist");
        Assertions.assertNotNull(u.getId(), "Test user id should not be null");
        testUserId = u.getId();
    }

    @AfterEach
    void cleanup() {
        for (Long id : createdIds) {
            try { importantDateService.deleteImportantDate(id); } catch (Exception ignore) {}
        }
        createdIds.clear();
    }

    @Test
    void sendWeekAndDayReminders_realEmail_shouldMarkBothWindows() {
        // Arrange two events: today+7 (week window) and today+1 (day window)
        LocalDate weekTarget = LocalDate.now().plusDays(7);
        LocalDate dayTarget = LocalDate.now().plusDays(1);

        ImportantDate weekDate = importantDateService.addImportantDate(
                testUserId,
                "REAL-EMAIL-WEEK-" + System.currentTimeMillis(),
                weekTarget,
                "custom",
                "real email week reminder"
        );
        ImportantDate updWeek = new ImportantDate();
        updWeek.setId(weekDate.getId());
        updWeek.setTitle(weekDate.getTitle());
        updWeek.setDescription(weekDate.getDescription());
        updWeek.setDate(weekDate.getDate());
        updWeek.setType(weekDate.getType());
        updWeek.setRepeatCycle("yearly");
        updWeek.setEnabled(true);
        weekDate = importantDateService.updateImportantDate(weekDate.getId(), updWeek);
        createdIds.add(weekDate.getId());

        ImportantDate dayDate = importantDateService.addImportantDate(
                testUserId,
                "REAL-EMAIL-DAY-" + System.currentTimeMillis(),
                dayTarget,
                "custom",
                "real email day reminder"
        );
        ImportantDate updDay = new ImportantDate();
        updDay.setId(dayDate.getId());
        updDay.setTitle(dayDate.getTitle());
        updDay.setDescription(dayDate.getDescription());
        updDay.setDate(dayDate.getDate());
        updDay.setType(dayDate.getType());
        updDay.setRepeatCycle("yearly");
        updDay.setEnabled(true);
        dayDate = importantDateService.updateImportantDate(dayDate.getId(), updDay);
        createdIds.add(dayDate.getId());

        // Act: trigger once; both windows should be sent in the same run
        importantDateService.sendAllPendingReminders();

        // Assert: both records have their respective sent timestamps set
        final Long weekIdRef = weekDate.getId();
        final Long dayIdRef = dayDate.getId();
        List<ImportantDate> after = importantDateService.getImportantDatesByUser(testUserId);
        ImportantDate persistedWeek = after.stream().filter(d -> d.getId().equals(weekIdRef)).findFirst().orElse(null);
        ImportantDate persistedDay = after.stream().filter(d -> d.getId().equals(dayIdRef)).findFirst().orElse(null);

        Assertions.assertNotNull(persistedWeek, "Week-window record should exist");
        Assertions.assertNotNull(persistedDay, "Day-window record should exist");
        Assertions.assertNotNull(persistedWeek.getWeekReminderSent(), "week_reminder_sent should be set");
        Assertions.assertNotNull(persistedDay.getDayReminderSent(), "day_reminder_sent should be set");
    }
}


