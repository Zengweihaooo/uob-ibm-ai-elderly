# FamilyController Test Guide

## Overview
Complete unit test suite for FamilyController.

## Structure

```
src/test/java/com/example/demo/controller/
└── FamilyControllerTest.java  # Unit tests (Mock-based)
```

## Unit Tests
### Unit Test (FamilyControllerTest.java)

Characteristics:
- `@WebMvcTest` only loads web layer
- `@MockBean` for FamilyService
- Controller logic + HTTP responses
- Fast & isolated
- No DB / external dependencies

Coverage:
- Add contact (success/failure)
- Get all contacts
- Get one contact
- Update contact
- Delete contact
- Send message
- Emergency contacts
- Statistics
- Auth validation
- Data validation
- Error handling

## Run

Maven Wrapper:

```bash
cd uob-ibm-ai-elderly/springboot
cd uob-ibm-ai-elderly/springboot

./mvnw test
./mvnw test -Dtest=FamilyControllerTest
./mvnw test -Dtest=FamilyControllerTest#testAddFamilyContact_Success
```

Batch (Windows):

```bash
run-familytests.bat
run-familytests.bat
```

IDE: Right-click -> Run

## Sample Data

### Contact JSON

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

Auth Header:
```
Authorization: Bearer test-token
```

## Endpoints Tested

| HTTP | Endpoint | Description | Tested |
|------|----------|-------------|--------|
| POST | `/api/family/contacts` | Add contact | ✅ |
| GET | `/api/family/contacts` | List contacts | ✅ |
| GET | `/api/family/contacts/{id}` | Get contact | ✅ |
| PUT | `/api/family/contacts/{id}` | Update contact | ✅ |
| DELETE | `/api/family/contacts/{id}` | Delete contact | ✅ |
| POST | `/api/family/contacts/{id}/message` | Send message | ✅ |
| GET | `/api/family/emergency-contacts` | Emergency contacts | ✅ |
| GET | `/api/family/stats` | Statistics | ✅ |

## Scenarios
Success: add/list/update/delete/send
Errors: missing auth, invalid auth, missing fields, not found, service exception
Edge: empty list, multiple contacts, emergency filter

## Responses
Success: 200 + `{"success": true, ...}`
Failure: 400/401/404/500 + `{"success": false, "message": "Error description"}`

## Notes
1. In-memory reset per test
2. Simple Bearer token (JWT later)
3. Email disabled
4. Each test isolated
5. Mockito mock service

## Extend
Boundary cases, relationships, notification prefs, concurrency, performance

## Troubleshooting
Service impl, dependencies, auth header

## Debug
Print logs, debugger, review output

## Future
After DB: service/repo/db/transaction tests
With external services: integration/E2E/performance/load