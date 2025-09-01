# Important Date Reminder - README

## Overview

The Important Date Reminder feature adds intelligent email reminders to the IBM AI Elderly Companion System. It can automatically send reminder emails one week and one day before each important date.

## Key Features

### 🎯 Core Capabilities
- **Dual Reminders**: Send reminder emails one week before and one day before the important date
- **Smart Date Handling**: Automatically supports annually recurring dates (e.g., birthdays, anniversaries)
- **Email Templates**: Beautiful HTML templates with content customized by reminder type
- **Scheduled Tasks**: Automatically checks and sends pending reminder emails every day at 8:00 AM

### 📧 Reminder Types
1. **One-week prior reminder**: Reminds users to prepare arrangements and gifts
2. **One-day prior reminder**: Reminds users to confirm the schedule for the day

### 📊 Management Functions
- Add, edit, delete important dates
- View upcoming important dates
- Get statistics for important dates
- Manually trigger reminder emails
- Add default holidays (e.g., New Year, Christmas)

## Technical Implementation

### Core Components

#### 1. Data Model (`ImportantDate.java`)
```java
public class ImportantDate {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private LocalDate date;
    private String type; // birthday, anniversary, holiday, custom
    private String repeatCycle; // none, yearly
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Reminder tracking
    private LocalDateTime weekReminderSent;
    private LocalDateTime dayReminderSent;
}
```

#### 2. Service Layer (`ImportantDateService.java`)
- Manage CRUD operations for important dates
- Compute the next occurrence of an important date
- Send reminder emails
- Retrieve important dates that require reminders

#### 3. Email Service (`EmailService.java`)
- Send important date reminder emails
- Generate HTML emails using Thymeleaf templates
- Support different reminder types (week/day)

#### 4. Scheduler (`ScheduleMonitorService.java`)
```java
@Scheduled(cron = "0 0 8 * * ?") // Run at 8:00 AM every day
public void checkImportantDateReminders() {
    importantDateService.sendAllPendingReminders();
}
```

### Email Template

Use `importantDateReminderTemplate.html`, which includes:
- Responsive design for mobile devices
- Dynamic content based on reminder type
- Elder-friendly UI design

## API Endpoints

### Important Date Management

#### Add Important Date
```http
POST /api/important-dates/add
Content-Type: application/json

{
    "userId": 1,
    "title": "Birthday",
    "date": "2025-01-15",
    "type": "birthday",
    "description": "My birthday"
}
```

#### Get User's Important Dates
```http
GET /api/important-dates/user/{userId}
```

#### Get Upcoming Important Dates
```http
GET /api/important-dates/user/{userId}/upcoming
```

#### Get Statistics
```http
GET /api/important-dates/user/{userId}/stats
```

#### Update Important Date
```http
PUT /api/important-dates/{id}
Content-Type: application/json

{
    "title": "Updated Title",
    "date": "2025-01-15",
    "type": "birthday",
    "description": "Updated description",
    "repeatCycle": "yearly",
    "enabled": true
}
```

#### Delete Important Date
```http
DELETE /api/important-dates/{id}
```

#### Toggle Important Date Status
```http
POST /api/important-dates/{id}/toggle
```

### Reminder Management

#### Manually Send Reminders
```http
POST /api/important-dates/send-reminders
```

#### Add Default Holidays
```http
POST /api/important-dates/user/{userId}/default-holidays
```

## Test Page

Visit `http://localhost:8080/important-date-test` to open the test page, which includes:

1. **Add Important Date**: Test adding new important dates
2. **View Important Dates**: View all important dates for the user
3. **Upcoming Dates**: View upcoming important dates
4. **Statistics**: View statistics for important dates
5. **Send Reminders**: Manually trigger reminder emails
6. **Add Default Holidays**: Add system-predefined holidays

## Configuration

### Mail Configuration
Configure SMTP in `application.properties`:

```properties
# SMTP Email Configuration
spring.mail.host=smtp.163.com
spring.mail.port=465
spring.mail.username=your-email@163.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.ssl.enable=true
spring.mail.default-encoding=UTF-8
```

### Scheduler Configuration
The scheduler runs at 8:00 AM daily by default. Adjust via cron expression:

```java
@Scheduled(cron = "0 0 8 * * ?") // Every day at 08:00
```

## Usage Flow

### 1. Add Important Date
Users can add important dates via API or the test page. The system records:
- Date information
- Recurrence cycle (yearly)
- Reminder status

### 2. Auto Reminders
Every day at 8:00 AM, the system checks:
- Dates requiring a one-week prior reminder
- Dates requiring a one-day prior reminder
- Sends the corresponding reminder emails

### 3. Email Content
- **One-week prior reminder**: Includes preparation suggestions
- **One-day prior reminder**: Includes confirmation checklist and last reminder

### 4. Status Tracking
Reminder timestamps are recorded to avoid duplicate sends:
- `weekReminderSent`: Timestamp of week reminder
- `dayReminderSent`: Timestamp of day reminder

## Notes

1. **Email Sending**: Ensure mail server configuration is correct; test sending
2. **User Email**: Ensure users have valid email addresses
3. **Timezone**: The system uses the server's local timezone
4. **Duplicate Prevention**: The system avoids duplicate reminders
5. **Data Persistence**: In-memory for now; recommend database in production

## Extensions

### Potential Enhancements
1. **SMS Reminders**: Add SMS support
2. **Push Notifications**: Integrate mobile push
3. **Custom Reminder Time**: Allow users to set reminder time
4. **Multi-language Support**: Support multiple languages for templates
5. **Reminder History**: Record and view reminder history

### Database Integration
Currently in-memory; can be easily integrated with an existing DB:

```sql
-- Important Dates Table
CREATE TABLE important_dates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    date DATE NOT NULL,
    type TEXT NOT NULL,
    repeat_cycle TEXT DEFAULT 'yearly',
    enabled BOOLEAN DEFAULT 1,
    week_reminder_sent DATETIME,
    day_reminder_sent DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id)
);
```

## Troubleshooting

### Common Issues

1. **Email sending failed**
   - Check SMTP server configuration
   - Verify recipient email address
   - Check application logs

2. **Scheduler not running**
   - Ensure `@EnableScheduling` is enabled
   - Verify cron expression format
   - Check application startup logs

3. **Incorrect important date calculation**
   - Check date formats
   - Verify timezone configuration
   - Validate recurrence logic

### Viewing Logs
The system prints detailed logs to the console:
- Email sending status
- Scheduler execution
- Error details

## Summary

The Important Date Reminder provides caring reminders for elderly users. Through intelligent email reminder mechanisms, users won’t miss important dates. The design considers elderly users’ habits, offering a simple interface and clear reminder content. 