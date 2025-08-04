# Pet Emotion Expression System

## Overview

The Pet Emotion Expression System is a sophisticated feature that simulates realistic pet behavior and emotional responses. The pet will not randomly move around the house but will express emotions through sounds and visual cues, responding to user interactions and showing signs of neglect when ignored.

## Key Features

### 🎭 Emotional States
- **Happy**: When receiving attention and care
- **Sad**: When neglected for extended periods
- **Anxious**: When moderately neglected
- **Excited**: During play interactions
- **Calm**: When content and relaxed

### 🔊 Sound Expressions
- **Dogs**: Barking, whining, excited barking, nervous barking, tail wagging sounds
- **Cats**: Meowing, purring, sad meowing, anxious meowing, excited meowing
- **Realistic Timing**: 30% chance to make sounds, making behavior more natural

### 👁️ Visual Expressions
- **Dogs**: Tail wagging, droopy ears, ears back, perked ears
- **Cats**: Bright eyes, droopy ears, flattened ears, alert eyes
- **Universal**: Relaxed posture, neutral expression

### 🚶 Movement Behavior
- **Controlled Movement**: Pet doesn't randomly wander around the house
- **Contextual Movement**: Only moves when excited, happy, or neglected
- **Movement Types**: Running (excited), walking (happy), pacing (neglected), sitting (calm)
- **Stay Command**: Can force pet to stay in place

### 📍 Location Management
- **Fixed Locations**: Pet stays in designated areas (living room, bedroom, kitchen, garden)
- **No Random Movement**: Pet doesn't move between rooms without reason
- **Location Tracking**: System tracks current location

## API Endpoints

### 1. Get Pet's Emotional State
```
GET /api/pet-emotion/state
```
Returns the pet's current emotional state, including happiness, energy, health, and neglect level.

### 2. Interact with Pet
```
POST /api/pet-emotion/interact
Content-Type: application/json

{
    "type": "pet|play|feed|talk",
    "message": "Optional message"
}
```
Processes interaction and returns pet's emotional response.

### 3. Get Pet's Expressions
```
GET /api/pet-emotion/expressions
```
Returns current sound and visual expressions.

### 4. Check Attention Status
```
GET /api/pet-emotion/attention-check
```
Checks if pet needs attention based on neglect level.

### 5. Get Movement Status
```
GET /api/pet-emotion/movement
```
Returns current movement and location information.

### 6. Make Pet Stay
```
POST /api/pet-emotion/stay
```
Forces pet to stay in place, preventing movement.

## Emotional Logic

### Neglect System
- **Neglect Level**: 0-100 scale
- **Increase Rate**: 5 points per hour of neglect
- **Reduction**: 15 points per interaction
- **Attention Threshold**: 30+ points triggers "needs attention"

### Happiness System
- **Increase**: 5-15 points per interaction
- **Decrease**: 1-3 points when neglected (if neglect > 20)
- **Range**: 0-100

### Energy System
- **Natural Decrease**: 1-3 points over time
- **Increase**: When happy and active
- **Range**: 0-100

## Pet Types Supported

### Dogs
- **Sounds**: Barking, whining, excited barking, nervous barking
- **Expressions**: Tail wagging, droopy ears, ears back, perked ears
- **Default**: Golden Retriever

### Cats
- **Sounds**: Meowing, purring, sad meowing, anxious meowing, excited meowing
- **Expressions**: Bright eyes, droopy ears, flattened ears, alert eyes
- **Default**: Domestic cat

## Testing

Run the test script to verify all functionality:

```bash
cd springboot
./test-pet-emotion-api.sh
```

The test script will:
1. Check initial emotional state
2. Test all interaction types
3. Verify expressions and movements
4. Test attention system
5. Validate stay command

## Configuration

### Default Pet Settings
- **Name**: Buddy
- **Type**: Dog
- **Breed**: Golden Retriever
- **Age**: 3 years
- **Initial Stats**: Happiness 80, Energy 70, Health 90

### Emotional Thresholds
- **Neglect Warning**: 30 points
- **Sad Threshold**: 50 points
- **Anxious Threshold**: 30 points
- **Calm Threshold**: 10 points

## Integration

This system integrates with the existing PetController and can be extended to:
- Send notifications when pet needs attention
- Integrate with smart home systems
- Connect to pet monitoring devices
- Provide analytics on pet behavior patterns

## Future Enhancements

1. **Multiple Pets**: Support for multiple pets per user
2. **Pet Personalities**: Different personality traits affecting behavior
3. **Environmental Factors**: Weather, time of day affecting pet mood
4. **Health Integration**: Connect with health monitoring systems
5. **Voice Commands**: Voice interaction with pets
6. **Pet Training**: Training commands and responses
7. **Social Features**: Pet interaction with other pets

## Technical Implementation

### Services
- **PetEmotionService**: Core emotional logic and behavior management
- **PetEmotionController**: REST API endpoints

### Data Model
- **Pet**: Complete pet entity with emotional and behavioral attributes
- **In-Memory Storage**: For demo purposes (can be replaced with database)

### Dependencies
- Spring Boot Web
- Spring Boot Test (for testing)
- Jackson (JSON processing)

## Security Considerations

- JWT token integration for user authentication
- Input validation for all API endpoints
- Rate limiting for interaction endpoints
- Data privacy for pet information

## Performance

- Lightweight emotional calculations
- Efficient in-memory storage
- Minimal API response times
- Scalable architecture for multiple users 