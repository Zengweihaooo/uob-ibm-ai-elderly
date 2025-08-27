# HealthController Feature Documentation

## Overview
HealthController is the health data management controller for the IBM AI Elderly project, providing health record addition, query, and anomaly detection functionality, integrated with an email notification system.

## Main Features

### 1. Health Record Management
- **Add Health Record**: POST `/api/health/record`
- **Query Today's Records**: GET `/api/health/today`
- **Get Statistics**: GET `/api/health/stats`

### 2. Anomaly Detection
The system automatically detects abnormal values for the following health data:

#### Blood Pressure (bloodPressure)
- **Normal Range**: Systolic 90-140 mmHg, Diastolic 60-90 mmHg
- **Abnormal Values**: Systolic < 90 or > 140, Diastolic < 60 or > 90
- **Input Format**: "120/80"

#### Blood Sugar (bloodSugar)
- **Normal Range**: 70-200 mg/dL
- **Abnormal Values**: < 70 or > 200
- **Input Format**: "120"

#### Daily Steps (steps)
- **Normal Range**: 1000-20000 steps/day
- **Abnormal Values**: < 1000 or > 20000
- **Input Format**: "8000"

### 3. Email Notification System
When abnormal values are detected, the system will:
1. Automatically send emails to emergency contacts
2. Return anomaly alerts in API responses
3. Log anomaly events

## API Endpoints

### POST `/api/health/record`
Add health record

**Headers**:
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "type": "bloodPressure|bloodSugar|steps",
  "value": "120/80"
}
```

**Response Example**:
```json
{
  "success": true,
  "record": {
    "id": 1,
    "userId": 1,
    "type": "bloodPressure",
    "value": "120/80",
    "recordTime": "2025-07-30T13:30:00",
    "notes": null
  },
  "abnormal": false
}
```

**Abnormal Value Response**:
```json
{
  "success": true,
  "record": {...},
  "alert": "Abnormal value detected! Email notification has been sent to emergency contact.",
  "abnormal": true
}
```

### GET `/api/health/today`
Get today's health records

**Response Example**:
```json
{
  "success": true,
  "records": [
    {
      "id": 1,
      "userId": 1,
      "type": "bloodPressure",
      "value": "120/80",
      "recordTime": "2025-07-30T13:30:00"
    }
  ]
}
```

### GET `/api/health/stats`
Get health statistics

**Response Example**:
```json
{
  "success": true,
  "totalRecords": 5,
  "abnormalCount": 2,
  "normalCount": 3
}
```

## Email Notification Features

### Email Template
- **Template File**: `healthAlertTemplate.html`
- **Design**: Responsive design, supports mobile viewing
- **Content**: Includes anomaly details, timestamps, and action recommendations

### Email Content
- Anomaly data type and value
- Detection time
- User ID
- Emergency contact recommendations

## Testing

### Run Test Script
```bash
cd springboot
./test-health-api.sh
```

### Manual Testing
```bash
# Test normal blood pressure
curl -X POST http://localhost:8080/api/health/record \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{"type": "bloodPressure", "value": "120/80"}'

# Test abnormal blood pressure (will trigger email)
curl -X POST http://localhost:8080/api/health/record \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{"type": "bloodPressure", "value": "160/95"}'
```

## Configuration

### Emergency Contact Email
Configure in `HealthService.java`:
```java
private final String emergencyContactEmail = "family@example.com";
```

### Email Server Configuration
Configure SMTP settings in `application.properties`.

## Important Notes

1. **User Authentication**: Currently uses hardcoded userId (1L), should extract from JWT token in production
2. **Data Storage**: Currently uses in-memory storage, database configuration needed for production
3. **Email Sending**: Ensure SMTP configuration is correct, test email sending functionality
4. **Error Handling**: System includes complete exception handling and error responses

## Future Enhancements

Consider adding in the future:
- Health data trend analysis
- Personalized health recommendations
- Multi-user support
- Database persistence
- Real-time notification push 