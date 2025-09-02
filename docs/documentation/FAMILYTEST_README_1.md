# FamilyController Test Guide

## Overview

This project provides a comprehensive unit test suite for the FamilyController.

## Test File Structure

```
src/test/java/com/example/demo/controller/
└── FamilyControllerTest.java              # Unit tests (Mock-based)
```

## Test Types

### Unit Test (FamilyControllerTest.java)

**Characteristics:**
- Uses `@WebMvcTest` to load only the web layer
- Uses `@MockBean` to mock FamilyService
- Tests controller logic and HTTP responses
- Fast and isolated
- No database/external dependencies

**Coverage:**
- ✅ Add family contact (success/failure)
- ✅ Get all contacts
- ✅ Get specific contact
- ✅ Update contact
- ✅ Delete contact
- ✅ Send message
- ✅ Get emergency contacts
- ✅ Get statistics
- ✅ Authentication validation
- ✅ Data validation
- ✅ Error handling

## How to Run Tests

### Method 1: Maven Wrapper

```bash
cd uob-ibm-ai-elderly/springboot
cd uob-ibm-ai-elderly/springboot

./mvnw test
./mvnw test -Dtest=FamilyControllerTest
./mvnw test -Dtest=FamilyControllerTest#testAddFamilyContact_Success
```

### Method 2: Batch Script (Windows)

```bash
run-familytests.bat
run-familytests.bat
```

### Method 3: IDE
Right-click class or method -> Run Test

## Test Data

### Sample Contact JSON

```json
{
  "name": "Zhang San",
  "phoneNumber": "13800138000",
  "email": "zhangsan@example.com",
  "relationship": "CHILD",
  "notificationPreference": "ALL",
  "isEmergencyContact": true,
  "notes": "My son"
}
```

### Authorization Header
```
Authorization: Bearer test-token
```

## API Endpoints Covered

| HTTP | Endpoint | Description | Tested |
|------|----------|-------------|--------|
| POST | `/api/family/contacts` | Add contact | ✅ |
| GET  | `/api/family/contacts` | Get all contacts | ✅ |
| GET  | `/api/family/contacts/{id}` | Get specific contact | ✅ |
| PUT  | `/api/family/contacts/{id}` | Update contact | ✅ |
| DELETE | `/api/family/contacts/{id}` | Delete contact | ✅ |
| POST | `/api/family/contacts/{id}/message` | Send message | ✅ |
| GET | `/api/family/emergency-contacts` | Get emergency contacts | ✅ |
| GET | `/api/family/stats` | Get statistics | ✅ |

## Test Scenarios

### Success
- Add contact
- List contacts
- Update contact
- Delete contact
- Send message

### Error
- Missing auth header
- Invalid auth header
- Missing required field
- Contact not found
- Service exception

### Edge
- Empty list
- Multiple contacts
- Emergency filtering

## Result Interpretation

### Success
- 200 OK
- `{"success": true, "message": "...", "data": {...}}`

### Failure
- 400 / 401 / 404 / 500
- `{"success": false, "message": "Error description"}`

## Notes
1. In-memory reset each test
2. Simple Bearer token (JWT later)
3. Email sending disabled
4. Isolation per test
5. Mockito mocks service

## Extending
1. More boundary tests
2. Relationship variants
3. Notification preferences
4. Concurrency
5. Performance metrics

## Troubleshooting
1. Failures: check FamilyService implementation
2. Compilation errors: verify dependencies
3. Auth failures: check header format

### Debug Tips
1. `System.out.println()`
2. IDE debugger
3. Review logs

## Roadmap
### After DB integration
1. Service tests
2. Repository tests
3. DB integration tests
4. Transaction tests
### With external services
1. External integration tests
2. End-to-end tests
3. Performance tests
4. Load tests