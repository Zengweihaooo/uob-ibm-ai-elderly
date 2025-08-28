# 📝 Memo App User Guide

## 🚀 Quick Start

### 1. Start the Application
```bash
./start-memo-app.sh
```

### 2. Access the Memo Page
Open your browser and visit: http://localhost:8080/pages/memo.html

### 3. First Login PIN Setup
- On first visit, the system will require you to set a PIN code
- The PIN code is used to protect important memos
- After setup, you can start using the memo features

## 🔐 New Features

### First Login PIN Setup
✅ **Mandatory PIN Setup**: Users must set a PIN code on first login
- 4-digit numeric PIN code
- Used to protect important memos
- Cannot be skipped after setup

### Important Memo (Important Memo)
❗ **Important Memo Features**:
- Automatically encrypted protection
- PIN code required for viewing/editing
- Only accessible with correct PIN code
- Identified with ❗ icon

## 📋 Features

### Memo Types
- **📄 General Memo**: Regular memos (no PIN required)
- **❗ Important Memo**: Important memos (PIN verification required)
- **📝 Todo Item**: Todo items

## 🔐 PIN Verification Process

### View Important Memo
1. Click the "🔓 View" button on an important memo
2. Enter 4-digit PIN code
3. Content displayed after successful verification

### Edit Important Memo
1. Click the "✏️ Edit" button on an important memo
2. Enter 4-digit PIN code
3. Enter edit mode after successful verification

### PIN Requirements
- Minimum 4 digits
- Set on first login
- Used for all important memos

## 📋 Features

### Search Function
- Search by title and content
- Real-time search results

### Statistics
- Display count statistics for different memo types
- Includes general, important, and todo item counts

## 🔧 Technical Architecture

### Frontend
- **Location**: `springboot/src/main/resources/static/`
- **Technology**: HTML5 + CSS3 + JavaScript
- **API Calls**: RESTful API

### Backend
- **Framework**: Spring Boot 3.4.7
- **Port**: 8080
- **API Path**: `/api/memo/*`

### Data Storage
- **Current**: In-memory storage (for demo)
- **Planned**: SQLite database

## 🐛 Common Issues

### Q: First login PIN setup fails
**A**: Make sure to access via http://localhost:8080/pages/memo.html, not by opening the file directly

### Q: Cannot view/edit important memos
**A**: Ensure the entered PIN code matches the one set on first login

### Q: Application won't start
**A**: Check if Java and Maven are properly installed

### Q: Page shows 404
**A**: Ensure Spring Boot application is running and static files are properly copied

### Q: API calls fail
**A**: Check browser console for CORS errors and ensure correct URL is used

## 📞 Technical Support

If you encounter issues, please check:
1. Spring Boot application is running
2. Browser console has no error messages
3. Network connection is normal
4. Port 8080 is not occupied

## 🔄 Update Log

### v2.0 New Features
- ✅ Mandatory first login PIN setup
- ✅ Important memo type (Important Memo)
- ✅ PIN verification protection for important memos
- ✅ Improved user interface and experience
- ✅ More secure memo protection mechanism
- ✅ English interface

---

**Note**: This is a demo project with in-memory data storage. Data will be lost after application restart. 