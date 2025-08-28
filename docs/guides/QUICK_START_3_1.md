# 🚀 Quick Start Guide for Memo Function

## 📋 Feature Overview

This is a memo function designed for elderly users, supporting:
- ✅ Unclassified, anytime recording of small memos
- ✅ Three types: General memos, Password memos, Todo items
- ✅ PIN code protection for sensitive information
- ✅ Search functionality
- ✅ Statistics information
- ✅ Elderly-friendly interface design

## 🛠️ Environment Requirements

- **Java**: Version 8 or higher
- **Maven**: Version 3.6 or higher
- **Browser**: Modern browsers like Chrome, Firefox, Safari, Edge

## ⚡ Quick Start

### Method 1: Using Startup Script (Recommended)

#### For macOS/Linux users:
```bash
./start-memo-demo.sh
```

#### For Windows users:
```cmd
start-memo-demo.bat
```

### Method 2: Manual Startup

1. **Enter project directory**
```bash
cd springboot
```

2. **Compile project**
```bash
mvn clean compile
```

3. **Start application**
```bash
mvn spring-boot:run
```

4. **Access pages**
- Main page: http://localhost:8080/src/pages/memo.html
- Test page: http://localhost:8080/src/pages/memo-test.html

## 📱 Usage Instructions

### 1. Create Memo
1. Click the "➕ New Memo" button
2. Fill in title and content
3. Select type (General/Password/Todo)
4. Click "Save"

### 2. Set PIN Code
1. Click the "🔐 Set PIN Code" button
2. Enter 4-digit PIN code
3. Click "Set PIN Code"

### 3. View Encrypted Memo
1. Click the "🔓 View" button on encrypted memos
2. Enter PIN code
3. View content

### 4. Search Memos
1. Enter keywords in the search box
2. System automatically displays matching results

### 5. Edit/Delete Memos
- Click "✏️ Edit" to modify memos
- Click "🗑️ Delete" to delete memos

## 🔧 API Interfaces

### Create Memo
```bash
curl -X POST http://localhost:8080/api/memo/create \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My Memo",
    "content": "Memo content",
    "type": "general"
  }'
```

### Get Memo List
```bash
curl http://localhost:8080/api/memo/list
```

### Search Memos
```bash
curl "http://localhost:8080/api/memo/search?keyword=keyword"
```

### Set PIN Code
```bash
curl -X POST http://localhost:8080/api/memo/set-pin \
  -H "Content-Type: application/json" \
  -d '{"pinCode": "1234"}'
```

## 📊 Feature Characteristics

### 🎯 Elderly-Friendly Design
- **Large Fonts**: Easy to read
- **High Contrast**: Clear visual hierarchy
- **Simple Operations**: One-click operation interface
- **Icon Assistance**: Use emoji icons to enhance readability

### 🔒 Security Features
- **PIN Code Protection**: 4-digit PIN code
- **Encrypted Display**: Sensitive information automatically encrypted
- **Input Validation**: Frontend and backend dual validation
- **Soft Delete**: Data won't be truly lost

### 📱 Responsive Design
- **Mobile Adaptation**: Support for phones and tablets
- **Touch-Friendly**: Button sizes suitable for touch operations
- **Adaptive Layout**: Adapt to different screen sizes

## 🧪 Testing Features

Access test page: http://localhost:8080/src/pages/memo-test.html

Test page includes:
- ✅ Create memo test
- ✅ Get memo list test
- ✅ Search function test
- ✅ PIN code function test
- ✅ Statistics test

## 🐛 Common Issues

### Q: Page cannot load
**A**: Check if server is started, confirm access address is correct

### Q: Memo cannot save
**A**: Check if title and content are filled, confirm network connection is normal

### Q: PIN code verification fails
**A**: Confirm PIN code is 4 digits, check if PIN code is correct

### Q: Search returns no results
**A**: Confirm search keyword is correct, check if memo exists

## 📁 Project Structure

```
uob-ibm-ai-elderly/
├── springboot/                    # Spring Boot Backend
│   ├── src/main/java/com/example/demo/
│   │   ├── controller/
│   │   │   └── MemoController.java    # Memo Controller
│   │   ├── service/
│   │   │   └── MemoService.java       # Memo Service
│   │   └── pojo/
│   │       └── Memo.java              # Memo Entity
│   └── pom.xml
├── src/pages/
│   ├── memo.html                     # Memo Main Page
│   └── memo-test.html                # Test Page
├── MEMO_README.md                    # Detailed Documentation
├── QUICK_START.md                    # Quick Start Guide
├── start-memo-demo.sh                # Linux/macOS Startup Script
└── start-memo-demo.bat               # Windows Startup Script
```

## 🚀 Extension Features

### Data Persistence
- Integrate MySQL/PostgreSQL database
- Add Redis cache layer
- Implement data backup functionality

### User Authentication
- Integrate JWT Token authentication
- Add user login/registration
- Support multi-user system

### Advanced Features
- Memo categorization and tags
- Reminder functionality
- Voice input
- Export functionality
- Cloud synchronization

## 📞 Technical Support

For questions or suggestions, please contact the development team.

---

**🎉 Enjoy using the memo function!** 