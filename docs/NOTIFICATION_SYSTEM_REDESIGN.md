# 通知系统重新设计文档

## 📋 概述

根据用户需求，我们重新设计了通知系统，简化了通知类型，并明确了Emergency Contact和Notification Preference的职责分离。

## 🔄 变更内容

### 1. **通知偏好类型简化**

**之前的5个类型** → **现在的4个类型**：

| 旧版本 | 新版本 | 说明 |
|--------|--------|------|
| `ALL` | `ALL` | 接收所有通知 |
| `EMERGENCY_ONLY` | ❌ **删除** | Emergency通知现在只由`isEmergencyContact`控制 |
| `HEALTH_ONLY` | `HEALTH_ALERT` | 仅接收健康警报 |
| `DAILY_SUMMARY` | `DAILY_SUMMARY` | 仅接收日常总结 |
| `NONE` | `NONE` | 不接收任何通知 |

### 2. **Emergency Contact与Notification Preference分离**

#### **Emergency通知** (由`isEmergencyContact`字段控制)
- **特点**: 紧急情况通知 **忽略** `notificationPreference` 设置
- **触发**: 只要 `isEmergencyContact = true`，就会收到紧急通知
- **用途**: 紧急医疗、跌倒检测、SOS等关键情况

#### **普通通知** (由`notificationPreference`字段控制)  
- **Health Alert**: 健康数据异常警报
- **Daily Summary**: 日常活动总结报告
- **General**: 一般性消息通知

## 🛠️ 技术实现

### 1. **数据库层面**

#### **Schema更新**
```sql
-- 添加notification_preference字段
ALTER TABLE family_contacts 
ADD COLUMN notification_preference VARCHAR(20) DEFAULT 'ALL';

-- 添加索引优化查询性能
CREATE INDEX idx_family_contacts_notification 
ON family_contacts(user_id, notification_preference);
```

#### **测试数据示例**
```sql
INSERT INTO family_contacts (user_id, name, relationship, phone, email, notification_preference, is_emergency_contact) 
VALUES (1, 'Zhang Xiaoming', 'Son', '+86 138 0013 8000', 'xiaoming@example.com', 'ALL', 1);

INSERT INTO family_contacts (user_id, name, relationship, phone, email, notification_preference, is_emergency_contact) 
VALUES (1, 'Dr. Wang', 'Doctor', '+86 137 0013 7000', 'doctor.wang@hospital.com', 'HEALTH_ALERT', 0);
```

### 2. **实体类更新**

#### **FamilyContact.java**
```java
public class FamilyContact {
    // ...其他字段
    private String notificationPreference;  // 新增字段: ALL, HEALTH_ALERT, DAILY_SUMMARY, NONE
    private Boolean isEmergencyContact;     // 保持独立控制emergency通知
    
    // 默认构造函数设置默认值
    public FamilyContact() {
        this.notificationPreference = "ALL";  // 默认接收所有通知
        this.isEmergencyContact = false;
    }
}
```

### 3. **服务层重构**

#### **FamilyService.java - 新增通知过滤方法**
```java
/**
 * 根据通知偏好获取联系人
 */
public List<FamilyContact> getContactsByNotificationPreference(Long userId, List<String> notificationTypes) {
    return familyContactMapper.findActiveByUserId(userId).stream()
            .filter(contact -> shouldReceiveNotification(contact, notificationTypes))
            .collect(Collectors.toList());
}

/**
 * 判断联系人是否应该接收特定类型的通知
 */
private boolean shouldReceiveNotification(FamilyContact contact, List<String> notificationTypes) {
    String preference = contact.getNotificationPreference();
    
    // 处理空值，默认为ALL
    if (preference == null || preference.trim().isEmpty()) {
        preference = "ALL";
    }
    
    // NONE表示不接收任何通知
    if ("NONE".equals(preference)) {
        return false;
    }
    
    // ALL表示接收所有类型通知  
    if ("ALL".equals(preference)) {
        return true;
    }
    
    // 检查通知类型是否匹配偏好设置
    return notificationTypes.contains(preference);
}
```

#### **专门的通知发送方法**

**Emergency通知** (忽略preference):
```java
public int sendEmergencyNotification(Long userId, String emergencyType, String description) {
    // 紧急通知只由isEmergencyContact控制，忽略notificationPreference
    List<FamilyContact> emergencyContacts = getEmergencyContacts(userId);
    // ...发送逻辑
}
```

**Health Alert通知** (考虑preference):
```java
public int sendHealthAlert(Long userId, String healthData, String alertType) {
    // 只发送给preference为ALL或HEALTH_ALERT的联系人
    List<String> notificationTypes = List.of("HEALTH_ALERT");
    List<FamilyContact> eligibleContacts = getContactsByNotificationPreference(userId, notificationTypes);
    // ...发送逻辑
}
```

**Daily Summary通知** (考虑preference):
```java
public int sendDailySummary(Long userId, String summaryData) {
    // 只发送给preference为ALL或DAILY_SUMMARY的联系人
    List<String> notificationTypes = List.of("DAILY_SUMMARY");
    List<FamilyContact> eligibleContacts = getContactsByNotificationPreference(userId, notificationTypes);
    // ...发送逻辑
}
```

### 4. **前端更新**

#### **family.html - 通知偏好选择**
```html
<select id="notificationPreference" class="form-select">
    <option value="ALL">All Notifications</option>
    <option value="HEALTH_ALERT">Health Alerts Only</option>
    <option value="DAILY_SUMMARY">Daily Summary Only</option>
    <option value="NONE">No Notifications</option>
</select>

<!-- Emergency Contact独立控制 -->
<div class="checkbox-group">
    <input type="checkbox" id="isEmergencyContact">
    <label for="isEmergencyContact">Emergency Contact</label>
</div>
```

## 📊 使用场景示例

### **场景1: 家庭成员配置**
- **儿子** (Zhang Xiaoming):
  - `notificationPreference: "ALL"`
  - `isEmergencyContact: true`
  - **结果**: 接收所有类型通知 + 紧急通知

- **医生** (Dr. Wang):
  - `notificationPreference: "HEALTH_ALERT"`  
  - `isEmergencyContact: false`
  - **结果**: 只接收健康警报，不接收紧急通知

- **朋友** (Friend):
  - `notificationPreference: "DAILY_SUMMARY"`
  - `isEmergencyContact: false`  
  - **结果**: 只接收日常总结，不接收其他通知

### **场景2: 通知发送逻辑**

**发送健康警报时**:
```java
// 只有儿子和医生会收到
familyService.sendHealthAlert(userId, "血压异常: 180/90", "高血压警报");
```

**发送紧急通知时**:
```java  
// 只有儿子会收到（因为他是emergency contact）
familyService.sendEmergencyNotification(userId, "跌倒检测", "检测到老人跌倒");
```

**发送日常总结时**:
```java
// 只有儿子和朋友会收到
familyService.sendDailySummary(userId, "今日步数: 5000步, 心率正常...");
```

## ✅ 优势

1. **职责清晰**: Emergency通知和常规通知完全分离
2. **配置灵活**: 家庭成员可以精确控制想接收的通知类型  
3. **紧急保障**: 紧急情况下绝不会因偏好设置而漏掉重要通知
4. **用户友好**: 简化了通知选项，降低配置复杂度
5. **性能优化**: 数据库索引优化了通知偏好查询性能

## 🔧 迁移指南

### **现有数据迁移**
```sql
-- 为现有联系人设置默认通知偏好
UPDATE family_contacts 
SET notification_preference = 'ALL' 
WHERE notification_preference IS NULL;

-- 将之前的EMERGENCY_ONLY用户调整为健康警报
UPDATE family_contacts 
SET notification_preference = 'HEALTH_ALERT' 
WHERE notification_preference = 'EMERGENCY_ONLY';
```

### **代码迁移**
- 更新所有调用`addFamilyContact`的地方，添加`notificationPreference`参数
- 使用新的专门方法(`sendHealthAlert`, `sendDailySummary`)替代通用的消息发送
- 确保Emergency通知调用`sendEmergencyNotification`方法

## 📝 总结

这次重新设计实现了：
- ✅ **简化通知类型**: 从5个减少到4个
- ✅ **明确职责分离**: Emergency vs. 常规通知  
- ✅ **保障紧急性**: Emergency通知不受偏好设置影响
- ✅ **提升灵活性**: 用户可精确控制通知接收
- ✅ **优化性能**: 数据库查询和索引优化

通过这个设计，Emergency Contact功能专注于真正的紧急情况，而Notification Preference则处理日常的信息推送，两者职责清晰，互不干扰。
