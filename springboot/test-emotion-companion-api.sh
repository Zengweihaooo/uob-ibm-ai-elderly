#!/bin/bash

# Test script for Emotion Companion API
# This script tests the emotion companion emotion expression functionality

BASE_URL="http://localhost:8080/api/emotion-companion"

echo "🤖 Testing Emotion Companion API"
echo "================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ $2${NC}"
    else
        echo -e "${RED}✗ $2${NC}"
    fi
}

# Test 1: Get emotion companion's emotional state
echo -e "\n${BLUE}Test 1: Get Emotion Companion's Emotional State${NC}"
response=$(curl -s -X GET "$BASE_URL/state")
echo "Response: $response"
print_status $? "Get emotion companion emotional state"

# Test 2: Get emotion companion's expressions (sound and visual)
echo -e "\n${BLUE}Test 2: Get Emotion Companion's Expressions${NC}"
response=$(curl -s -X GET "$BASE_URL/expressions")
echo "Response: $response"
print_status $? "Get emotion companion expressions"

# Test 3: Check if emotion companion needs attention
echo -e "\n${BLUE}Test 3: Check Emotion Companion Attention Status${NC}"
response=$(curl -s -X GET "$BASE_URL/attention-check")
echo "Response: $response"
print_status $? "Check emotion companion attention"

# Test 4: Get emotion companion's activity status
echo -e "\n${BLUE}Test 4: Get Emotion Companion's Activity Status${NC}"
response=$(curl -s -X GET "$BASE_URL/activity")
echo "Response: $response"
print_status $? "Get emotion companion activity"

# Test 5: Interact with emotion companion - Chat
echo -e "\n${BLUE}Test 5: Interact with Emotion Companion - Chat${NC}"
response=$(curl -s -X POST "$BASE_URL/interact" \
    -H "Content-Type: application/json" \
    -d '{
        "type": "chat",
        "message": "Hello, how are you today?"
    }')
echo "Response: $response"
print_status $? "Emotion companion interaction - chat"

# Test 6: Interact with emotion companion - Command/Help
echo -e "\n${BLUE}Test 6: Interact with Emotion Companion - Command/Help${NC}"
response=$(curl -s -X POST "$BASE_URL/interact" \
    -H "Content-Type: application/json" \
    -d '{
        "type": "command",
        "message": "Can you help me with something?"
    }')
echo "Response: $response"
print_status $? "Emotion companion interaction - command"

# Test 7: Interact with emotion companion - Question
echo -e "\n${BLUE}Test 7: Interact with Emotion Companion - Question${NC}"
response=$(curl -s -X POST "$BASE_URL/interact" \
    -H "Content-Type: application/json" \
    -d '{
        "type": "question",
        "message": "What is the weather like today?"
    }')
echo "Response: $response"
print_status $? "Emotion companion interaction - question"

# Test 8: Interact with emotion companion - Greet
echo -e "\n${BLUE}Test 8: Interact with Emotion Companion - Greet${NC}"
response=$(curl -s -X POST "$BASE_URL/interact" \
    -H "Content-Type: application/json" \
    -d '{
        "type": "greet",
        "message": "Good morning!"
    }')
echo "Response: $response"
print_status $? "Emotion companion interaction - greet"

# Test 9: Put emotion companion to sleep
echo -e "\n${BLUE}Test 9: Put Emotion Companion to Sleep${NC}"
response=$(curl -s -X POST "$BASE_URL/sleep")
echo "Response: $response"
print_status $? "Put emotion companion to sleep"

# Test 10: Wake up emotion companion
echo -e "\n${BLUE}Test 10: Wake Up Emotion Companion${NC}"
response=$(curl -s -X POST "$BASE_URL/wake")
echo "Response: $response"
print_status $? "Wake up emotion companion"

# Test 11: Check emotional state after interactions
echo -e "\n${BLUE}Test 11: Check Emotional State After Interactions${NC}"
response=$(curl -s -X GET "$BASE_URL/state")
echo "Response: $response"
print_status $? "Check emotional state after interactions"

# Test 12: Check expressions after interactions
echo -e "\n${BLUE}Test 12: Check Expressions After Interactions${NC}"
response=$(curl -s -X GET "$BASE_URL/expressions")
echo "Response: $response"
print_status $? "Check expressions after interactions"

echo -e "\n${YELLOW}🎉 Emotion Companion API Testing Complete!${NC}"
echo -e "${YELLOW}The emotion companion should now show different emotional states, LED colors, and sounds based on interactions.${NC}" 