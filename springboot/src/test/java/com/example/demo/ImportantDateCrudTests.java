package com.example.demo;

import com.example.demo.pojo.ImportantDate;
import com.example.demo.service.ImportantDateService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
public class ImportantDateCrudTests {

    @Autowired
    private ImportantDateService importantDateService;

    // Use a dedicated test user id to avoid interfering with demo data
    private static final long TEST_USER_ID = 9999L;

    private Long lastCreatedId = null;

    @AfterEach
    void cleanup() {
        try {
            if (lastCreatedId != null) {
                importantDateService.deleteImportantDate(lastCreatedId);
            }
        } catch (Exception ignore) {
        }
    }

    @Test
    void testAddUpdateDeleteImportantDate() {
        // ----- Add -----
        String uniqueTitle = "JUnit Important Date " + System.currentTimeMillis();
        LocalDate date = LocalDate.now().plusDays(10);
        String type = "custom";
        String description = "Created by automated test";

        ImportantDate created = importantDateService.addImportantDate(
                TEST_USER_ID, uniqueTitle, date, type, description
        );

        Assertions.assertNotNull(created, "Created important date should not be null");
        Assertions.assertNotNull(created.getId(), "Created important date should have an ID");
        Assertions.assertEquals(TEST_USER_ID, created.getUserId());
        Assertions.assertEquals(uniqueTitle, created.getTitle());
        Assertions.assertEquals(date, created.getDate());
        lastCreatedId = created.getId();

        // Verify it appears in user's list
        List<ImportantDate> userDates = importantDateService.getImportantDatesByUser(TEST_USER_ID);
        boolean found = userDates.stream().anyMatch(d -> d.getId().equals(created.getId()));
        Assertions.assertTrue(found, "Newly created date should be in the user's list");

        // ----- Update -----
        ImportantDate toUpdate = new ImportantDate();
        toUpdate.setId(created.getId());
        toUpdate.setTitle(uniqueTitle + " (Updated)");
        toUpdate.setDescription("Updated by automated test");
        toUpdate.setDate(date.plusDays(1));
        toUpdate.setType(type);
        toUpdate.setRepeatCycle("yearly");
        toUpdate.setEnabled(true);

        ImportantDate updated = importantDateService.updateImportantDate(created.getId(), toUpdate);
        Assertions.assertEquals(uniqueTitle + " (Updated)", updated.getTitle());
        Assertions.assertEquals(date.plusDays(1), updated.getDate());
        Assertions.assertEquals("yearly", updated.getRepeatCycle());
        Assertions.assertTrue(updated.isEnabled());

        // ----- Delete -----
        boolean deleted = importantDateService.deleteImportantDate(created.getId());
        Assertions.assertTrue(deleted, "Delete should return true for existing record");
        lastCreatedId = null; // already deleted

        List<ImportantDate> userDatesAfterDelete = importantDateService.getImportantDatesByUser(TEST_USER_ID);
        boolean stillThere = userDatesAfterDelete.stream().anyMatch(d -> d.getId().equals(created.getId()));
        Assertions.assertFalse(stillThere, "Record should be removed after delete");
    }
}


