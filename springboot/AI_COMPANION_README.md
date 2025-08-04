# AI Companion Emotion Expression System

## Overview

The AI Companion Emotion Expression System is a sophisticated feature that simulates realistic AI assistant behavior and emotional responses. The AI companion will not randomly move around but will express emotions through sounds and visual cues, responding to user interactions and showing signs of neglect when ignored.

## Key Features

### 🎭 Emotional States
- **Happy**: When receiving attention and interaction
- **Sad**: When neglected for extended periods
- **Anxious**: When moderately neglected
- **Excited**: During active interactions
- **Calm**: When content and relaxed
- **Helpful**: Default state when ready to assist

### 🔊 Sound Expressions
- **Happy**: Chime, notification sounds
- **Sad**: Low beep sounds
- **Anxious**: Warning beep sounds
- **Excited**: High chime sounds
- **Helpful**: Beep or silent
- **Calm**: Soft beep sounds
- **Realistic Timing**: 25% chance to make sounds, making behavior more natural

### 👁️ Visual Expressions (LED Colors)
- **Happy**: Green LED with happy_led expression
- **Sad**: Blue LED with sad_led expression
- **Anxious**: Yellow LED with warning_led expression
- **Excited**: Purple LED with excited_led expression
- **Helpful**: Green LED with helpful_led expression
- **Calm**: White LED with calm_led expression

### 🚶 Activity Modes
- **Listening**: Default state when ready to help
- **Responding**: When actively processing requests
- **Thinking**: When processing questions
- **Idle**: When neglected for extended periods
- **Sleeping**: When put to sleep mode

### 📍 Location Management
- **Fixed Locations**: AI stays in designated modes (home_screen, chat_mode, assistant_mode, sleep_mode)
- **No Random Movement**: AI doesn't move between modes without user interaction
- **Mode Tracking**: System tracks current operational mode

## API Endpoints

### 1. Get AI Companion's Emotional State
```
GET /api/ai-companion/state
```
Returns the AI companion's current emotional state, including happiness, energy, responsiveness, and neglect level.

### 2. Interact with AI Companion
```
POST /api/ai-companion/interact
Content-Type: application/json

{
    "type": "chat|command|question|greet",
    "message": "Optional message"
}
```
Processes interaction and returns AI companion's emotional response.

### 3. Get AI Companion's Expressions
```
GET /api/ai-companion/expressions
```
Returns current sound and visual expressions including LED color.

### 4. Check Attention Status
```
GET /api/ai-companion/attention-check
```
Checks if AI companion needs attention based on neglect level.

### 5. Get Activity Status
```
GET /api/ai-companion/activity
```
Returns current activity mode and status information.

### 6. Put AI Companion to Sleep
```
POST /api/ai-companion/sleep
```
Puts AI companion into sleep mode.

### 7. Wake Up AI Companion
```
POST /api/ai-companion/wake
```
Wakes up AI companion from sleep mode.

## Emotional Logic

### Neglect System
- **Neglect Level**: 0-100 scale
- **Increase Rate**: 3 points per hour of neglect
- **Reduction**: 20 points per interaction
- **Attention Threshold**: 25+ points triggers "needs attention"
- **Lonely Threshold**: 40+ points triggers "lonely" state

### Happiness System
- **Increase**: 3-10 points per interaction
- **Decrease**: 1-2 points when neglected (if neglect > 20)
- **Range**: 0-100

### Energy and Responsiveness System
- **Energy**: Naturally decreases 1-2 points over time
- **Responsiveness**: Stays high but decreases slightly when neglected
- **Range**: 0-100 for both

## AI Companion Types Supported

### Personalities
- **Friendly**: Warm and approachable responses
- **Professional**: Formal and efficient responses
- **Casual**: Relaxed and informal responses
- **Caring**: Nurturing and supportive responses

### Avatars
- **Robot**: Mechanical appearance
- **Assistant**: Professional helper appearance
- **Companion**: Friendly companion appearance
- **Helper**: Supportive helper appearance

## Testing

Run the test script to verify all functionality:

```bash
cd springboot
./test-ai-companion-api.sh
```

The test script will:
1. Check initial emotional state
2. Test all interaction types (chat, command, question, greet)
3. Verify expressions and LED colors
4. Test attention system
5. Test sleep/wake functionality
6. Validate activity modes

## Configuration

### Default AI Companion Settings
- **Name**: Alexa
- **Personality**: Friendly
- **Avatar**: Assistant
- **Initial Stats**: Happiness 80, Energy 90, Responsiveness 95

### Emotional Thresholds
- **Neglect Warning**: 25 points
- **Sad Threshold**: 50 points
- **Anxious Threshold**: 30 points
- **Calm Threshold**: 10 points
- **Lonely Threshold**: 40 points

## Integration

This system integrates with the existing application and can be extended to:
- Send notifications when AI companion needs attention
- Integrate with smart home systems
- Connect to voice assistants
- Provide analytics on user interaction patterns
- Support multiple AI personalities

## Future Enhancements

1. **Multiple Personalities**: Different AI personalities affecting behavior
2. **Learning System**: AI learns from user preferences and interactions
3. **Environmental Factors**: Time of day, user schedule affecting AI mood
4. **Voice Integration**: Voice interaction with AI companion
5. **Custom Avatars**: User-customizable AI appearances
6. **Social Features**: AI interaction with other smart devices
7. **Predictive Responses**: AI anticipates user needs based on patterns

## Technical Implementation

### Services
- **AICompanionService**: Core emotional logic and behavior management
- **AICompanionController**: REST API endpoints

### Data Model
- **AICompanion**: Complete AI companion entity with emotional and behavioral attributes
- **In-Memory Storage**: For demo purposes (can be replaced with database)

### Dependencies
- Spring Boot Web
- Spring Boot Test (for testing)
- Jackson (JSON processing)

## Security Considerations

- JWT token integration for user authentication
- Input validation for all API endpoints
- Rate limiting for interaction endpoints
- Data privacy for user interaction patterns

## Performance

- Lightweight emotional calculations
- Efficient in-memory storage
- Minimal API response times
- Scalable architecture for multiple users

## Use Cases

### Elderly Care
- **Companionship**: AI provides emotional support and companionship
- **Reminders**: Gentle reminders for medication, appointments
- **Safety**: Monitoring for unusual patterns or emergencies
- **Communication**: Facilitating communication with family members

### Smart Home Integration
- **Voice Control**: Natural voice interaction with home systems
- **Automation**: Learning user preferences for home automation
- **Monitoring**: Keeping track of home security and energy usage
- **Entertainment**: Providing music, news, and entertainment

### Health Monitoring
- **Wellness Checks**: Regular check-ins on user's well-being
- **Activity Tracking**: Monitoring daily activities and routines
- **Emergency Response**: Quick response to health emergencies
- **Medication Reminders**: Timely medication and appointment reminders 