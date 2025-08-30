package com.example.demo;

import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.ImportantDate;
import com.example.demo.pojo.User;
import com.example.demo.service.ImportantDateService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class ImportantDateReminderDisableTests {

    @Autowired
    private ImportantDateService importantDateService;

    @Autowired
    private UserMapper userMapper;

    @MockBean
    private JavaMailSender mailSender;

    private Long testUserId;
    private Long testDateId;
    private final String testEmail = "1534435440@qq.com"; // 目标用户邮箱（仅用于数据准备）

    @BeforeEach
    void setupMailMock() {
        // 让模板发送链路可执行：提供一个可用的 MimeMessage 实例
        Mockito.when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage((Session) null));
    }

    @AfterEach
    void cleanup() {
        try {
            if (testDateId != null) {
                importantDateService.deleteImportantDate(testDateId);
            }
        } catch (Exception ignore) {}
        try {
            if (testUserId != null) {
                userMapper.deleteById(testUserId);
            }
        } catch (Exception ignore) {}
        testUserId = null;
        testDateId = null;
    }

    private void prepareTestUser() {
        // 如已存在相同邮箱用户，先删除以避免 UNIQUE(email) 冲突
        User existing = userMapper.findByEmail(testEmail);
        if (existing != null) {
            userMapper.deleteById(existing.getId());
        }

        User u = new User(testEmail);
        // 避免 users.password_hash NOT NULL 约束失败
        u.setPasswordHash("test_password");
        userMapper.insert(u);
        User loaded = userMapper.findByEmail(testEmail);
        Assertions.assertNotNull(loaded, "Test user should be inserted");
        testUserId = loaded.getId();
    }

    @Test
    void disabledImportantDate_shouldNotSendWeekReminder() {
        prepareTestUser();
        LocalDate target = LocalDate.now().plusDays(7);

        ImportantDate created = importantDateService.addImportantDate(
                testUserId,
                "Disable-Week-Reminder",
                target,
                "custom",
                "disabled record"
        );
        created.setEnabled(false);
        // 按照当前服务设计，updateImportantDate 负责持久化 enabled 变更
        ImportantDate toUpdate = new ImportantDate();
        toUpdate.setId(created.getId());
        toUpdate.setTitle(created.getTitle());
        toUpdate.setDescription(created.getDescription());
        toUpdate.setDate(created.getDate());
        toUpdate.setType(created.getType());
        toUpdate.setRepeatCycle("yearly");
        toUpdate.setEnabled(false);
        ImportantDate updated = importantDateService.updateImportantDate(created.getId(), toUpdate);
        testDateId = updated.getId();

        // 触发提醒扫描
        importantDateService.sendAllPendingReminders();

        // 验证：未发送邮件
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void enabledImportantDate_shouldSendWeekReminder() {
        prepareTestUser();
        LocalDate target = LocalDate.now().plusDays(7);

        ImportantDate created = importantDateService.addImportantDate(
                testUserId,
                "Enable-Week-Reminder",
                target,
                "custom",
                "enabled record"
        );
        created.setEnabled(true);
        ImportantDate toUpdate = new ImportantDate();
        toUpdate.setId(created.getId());
        toUpdate.setTitle(created.getTitle());
        toUpdate.setDescription(created.getDescription());
        toUpdate.setDate(created.getDate());
        toUpdate.setType(created.getType());
        toUpdate.setRepeatCycle("yearly");
        toUpdate.setEnabled(true);
        ImportantDate updated = importantDateService.updateImportantDate(created.getId(), toUpdate);
        testDateId = updated.getId();

        // 触发提醒扫描
        importantDateService.sendAllPendingReminders();

        // 验证：至少发送一次邮件
        verify(mailSender, atLeastOnce()).send(any(MimeMessage.class));
    }
}


