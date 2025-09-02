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
- Fast execution and good isolation
- No dependency on database or external services

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
# Enter project directory
cd uob-ibm-ai-elderly/springboot

# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=FamilyControllerTest

# Run a specific test method
./mvnw test -Dtest=FamilyControllerTest#testAddFamilyContact_Success
```

### Method 2: Batch Script (Windows)

```bash
# Double-click to run
run-familytests.bat
```

### Method 3: IDE

Right-click the test class or test method in your IDE and select "Run Test".

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

All secured APIs use the following header:
```
Authorization: Bearer test-token
```

## API Endpoints Covered by Tests

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
- ✅ Add contact
- ✅ Get contact list
- ✅ Update contact
- ✅ Delete contact
- ✅ Send message

### Error
- ✅ Missing auth header
- ✅ Invalid auth header
- ✅ Missing required field
- ✅ Contact not found
- ✅ Service exception

### Edge
- ✅ Empty list
- ✅ Multiple contacts
- ✅ Emergency contact filtering

## Result Interpretation

### Success Response
- HTTP Status: 200 OK
- Response: `{"success": true, "message": "...", "data": {...}}`

### Failure Response
- HTTP Status: 400 / 401 / 404 / 500
- Response: `{"success": false, "message": "Error description"}`

## Notes

1. In-memory storage reset before each test
2. Simple Bearer token auth (JWT later)
3. Real email sending disabled
4. Each test isolated
5. Mockito mocks FamilyService

## Extending Tests

You can add:
1. More boundary tests
2. Different relationship types
3. Notification preference variants
4. Concurrency scenarios
5. Performance metrics

## Troubleshooting

### Common Issues

1. Test failure: verify FamilyService implementation
2. Compilation error: check dependencies
3. Auth failure: check Authorization header format

### Debug Tips

1. Add `System.out.println()` for quick logs
2. Use IDE debugger
3. Inspect test logs

## Future Roadmap

### After database integration
1. Service layer unit tests
2. Repository tests
3. DB integration tests
4. Transaction tests

### With external services
1. External service integration tests
2. End-to-end tests
3. Performance tests
4. Load tests