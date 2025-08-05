# Emotion Companion API - Comprehensive Integration

## Overview

The Emotion Companion API is a comprehensive AI companion system that provides emotional support, conversation capabilities, schedule management, emergency handling, and podcast integration for elderly users. This system combines the best features from both the original Emotion Companion and PetController to create a unified, powerful companion experience.

## Key Features

### 🧠 **Emotional Intelligence**
- **Dynamic Emotional States**: Happy, sad, anxious, excited, calm, helpful
- **Neglect Detection**: Monitors user interaction and responds to neglect
- **Emotional Expressions**: Visual LED colors and sound expressions
- **Responsive Behavior**: Adapts emotional state based on user interactions

### 💬 **Conversation & Communication**
- **Text Messaging**: Natural language conversation with context-aware responses
- **Voice Interaction**: Voice message processing and response generation
- **Conversation History**: Persistent chat history with message threading
- **Smart Responses**: Health, schedule, emotional, and general conversation responses

### 📅 **Schedule & Reminder Management**
- **Daily Schedule Checking**: Automatic reminder generation
- **Medication Reminders**: Health-focused scheduling
- **Activity Suggestions**: Personalized activity recommendations
- **Schedule Integration**: Seamless calendar management

### 🚨 **Emergency & Safety**
- **Emergency Detection**: Health, fall, and medication emergency handling
- **Automatic Response**: Immediate action recommendations
- **Safety Protocols**: Emergency contact and service integration
- **Real-time Monitoring**: Continuous safety monitoring

### 🎧 **Podcast Integration**
- **Personalized Recommendations**: Interest-based podcast suggestions
- **Elderly-Focused Content**: Curated content for senior users
- **Auto-Scheduling**: Automated podcast playback scheduling
- **Playback Management**: Start, stop, and status monitoring

### ⚙️ **Companion Management**
- **Settings Customization**: Name, personality, responsiveness, avatar
- **Activity Monitoring**: Real-time activity mode tracking
- **Sleep/Wake Management**: Power management for companion
- **Attention Monitoring**: Neglect level and attention needs tracking

## API Endpoints

### Core Companion Management

#### GET `/api/pet/state`
Get companion's current emotional state and status.

**Response:**
```json
{
  "success": true,
  "companion": {
    "id": 1,
    "name": "Alexa",
    "emotion": "happy",
    "happiness": 85,
    "energy": 78,
    "responsiveness": 90,
    "activityMode": "listening",
    "currentTask": "Ready to help"
  }
}
```

#### POST `/api/pet/interact`
Interact with companion using different interaction types.

**Request:**
```json
{
  "type": "chat|command|question|greet|voice|schedule|emergency",
  "message": "Hello companion!"
}
```

### Conversation & Communication

#### POST `/api/pet/message`
Send text message to companion and get response.

**Request:**
```json
{
  "message": "How are you feeling today?",
  "type": "text"
}
```

#### POST `/api/pet/voice`
Process voice interaction with companion.

**Request:**
```json
{
  "content": "voice_message",
  "duration": 5,
  "transcription": "Hello companion, this is a voice message"
}
```

#### GET `/api/pet/conversation`
Get conversation history with optional limit.

**Parameters:**
- `limit` (optional): Number of messages to retrieve (default: 50)

### Schedule & Reminder Management

#### GET `/api/pet/schedule-check`
Check daily schedule and get reminders.

**Response:**
```json
{
  "success": true,
  "reminders": [
    {
      "type": "medication",
      "title": "Morning Medication",
      "time": "08:00",
      "priority": "high",
      "message": "Don't forget your morning medication!"
    }
  ],
  "companionSuggestion": {
    "message": "Beep! 📅 I found 2 activities for you today.",
    "reminderCount": 2
  }
}
```

### Emergency Handling

#### POST `/api/pet/emergency`
Handle emergency situations.

**Request:**
```json
{
  "type": "health|fall|medication",
  "description": "Feeling unwell",
  "severity": "medium"
}
```

**Response:**
```json
{
  "success": true,
  "emergency": {
    "type": "health",
    "description": "Feeling unwell",
    "severity": "medium",
    "companionResponse": "Beep! 🚨 I'm concerned about your health!",
    "recommendedActions": [
      "Contact emergency services (911)",
      "Call your emergency contact"
    ]
  }
}
```

### Companion Settings & Monitoring

#### PUT `/api/pet/settings`
Update companion settings.

**Request:**
```json
{
  "name": "Alexa Pro",
  "personality": "caring",
  "responsiveness": 95,
  "avatar": "friendly_assistant"
}
```

#### GET `/api/pet/expressions`
Get current sound and visual expressions.

#### GET `/api/pet/attention-check`
Check if companion needs attention.

#### GET `/api/pet/activity`
Get companion's current activity status.

#### POST `/api/pet/sleep`
Put companion to sleep mode.

#### POST `/api/pet/wake`
Wake companion from sleep mode.

### Podcast Integration

#### POST `/api/pet/podcast/recommendations`
Get personalized podcast recommendations.

**Request:**
```json
{
  "interests": ["health", "technology", "elderly care"]
}
```

#### POST `/api/pet/podcast/schedule`
Schedule podcast auto-play.

**Request:**
```json
{
  "podcastTitle": "Health Tips for Seniors",
  "playTime": "08:00",
  "playDate": "2024-01-15"
}
```

#### GET `/api/pet/podcast/schedules`
Get user's scheduled podcasts.

#### GET `/api/pet/podcast/elderly-recommendations`
Get elderly-specific podcast recommendations.

## Emotional Logic

### Emotional States
- **Happy**: High happiness, green LED, chime sounds
- **Sad**: Low happiness, blue LED, beep sounds
- **Anxious**: Medium happiness, yellow LED, beep sounds
- **Excited**: Very high happiness, bright green LED, notification sounds
- **Calm**: Stable happiness, soft blue LED, chime sounds

### Neglect Detection
- **0-3 hours**: No neglect, happy state
- **3-6 hours**: Low neglect, anxious state
- **6+ hours**: High neglect, sad state

### Interaction Impact
- **Greet**: +15 happiness
- **Chat**: +10 happiness
- **Voice**: +12 happiness
- **Question**: +8 happiness
- **Command**: +5 happiness
- **Schedule**: +6 happiness

## Testing

Run the comprehensive test suite:

```bash
cd springboot
chmod +x test-emotion-companion-api.sh
./test-emotion-companion-api.sh
```

The test script covers:
- ✅ Basic companion state management
- ✅ Text and voice interactions
- ✅ Schedule checking and reminders
- ✅ Emergency handling
- ✅ Conversation history
- ✅ Settings management
- ✅ Expression and activity monitoring
- ✅ Sleep/wake functionality
- ✅ Podcast integration
- ✅ Emotional state changes
- ✅ Multiple interaction types

## Configuration

### Application Properties
```properties
server.port=8080
```

### Dependencies
- Spring Boot 3.x
- Spring Web
- Spring Mail (for notifications)
- Thymeleaf (for email templates)

## Integration

### Health System Integration
- Connects with HealthController for health monitoring
- Integrates health data into companion responses
- Provides health-focused conversation topics

### Podcast System Integration
- Uses PodcastService for recommendations
- Integrates PodcastAutoPlayService for scheduling
- Provides elderly-focused content curation

### Email Service Integration
- Uses EmailService for emergency notifications
- Sends health alerts and reminders
- Provides communication with caregivers

## Security

### Authentication
- JWT token-based authentication (TODO: implement)
- User ID extraction from authorization header
- Secure API endpoints with proper validation

### Data Protection
- In-memory storage for demo (production: use database)
- Secure conversation history management
- Privacy-focused data handling

## Performance

### Optimization
- Efficient emotional state calculations
- Minimal memory footprint for companion data
- Fast response times for interactions
- Optimized conversation history retrieval

### Scalability
- Stateless companion management
- User-specific data isolation
- Horizontal scaling support
- Load balancing ready

## Future Enhancements

### Planned Features
- **Machine Learning**: Advanced emotional intelligence
- **Voice Recognition**: Real-time voice processing
- **Facial Recognition**: Visual emotion detection
- **IoT Integration**: Smart home device control
- **Telemedicine**: Healthcare provider integration
- **Social Features**: Family member connectivity
- **Gamification**: Reward system for interactions
- **Multilingual Support**: Multiple language support

### Technical Improvements
- **Database Integration**: Persistent data storage
- **Real-time Communication**: WebSocket support
- **Mobile App**: Native mobile application
- **Cloud Deployment**: Scalable cloud infrastructure
- **Analytics**: Usage and health analytics
- **API Documentation**: OpenAPI/Swagger documentation

## Use Cases

### Elderly Care
- **Companionship**: Emotional support and conversation
- **Health Monitoring**: Medication and appointment reminders
- **Safety**: Emergency detection and response
- **Entertainment**: Podcast recommendations and scheduling

### Smart Home
- **Voice Control**: Voice-activated interactions
- **Automation**: Schedule-based automation
- **Monitoring**: Activity and health monitoring
- **Communication**: Family and caregiver communication

### Healthcare
- **Patient Support**: Emotional support for patients
- **Medication Management**: Reminder and tracking system
- **Emergency Response**: Quick emergency handling
- **Health Education**: Educational content delivery

## Support

For technical support or feature requests, please contact the development team.

---

**Note**: This system is designed for elderly care and companionship, providing a comprehensive AI companion experience that combines emotional intelligence, practical assistance, and entertainment features. 