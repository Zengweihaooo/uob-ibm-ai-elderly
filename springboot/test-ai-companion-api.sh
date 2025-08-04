#!/bin/bash

# Test script for AI Companion API
# This script tests the AI companion emotion expression functionality

BASE_URL="http://localhost:8080/api/ai-companion"

echo "🤖 Testing AI Companion API"
echo "==========================="

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

# Test 1: Get AI companion's emotional state
echo -e "\n${BLUE}Test 1: Get AI Companion's Emotional State${NC}"
response=$(curl -s -X GET "$BASE_URL/state")
echo "Response: $response"
print_status $? "Get AI companion emotional state"

# Test 2: Get AI companion's expressions (sound and visual)
echo -e "\n${BLUE}Test 2: Get AI Companion's Expressions${NC}"
response=$(curl -s -X GET "$BASE_URL/expressions")
echo "Response: $response"
print_status $? "Get AI companion expressions"

# Test 3: Check if AI companion needs attention
echo -e "\n${BLUE}Test 3: Check AI Companion Attention Status${NC}"
response=$(curl -s -X GET "$BASE_URL/attention-check")
echo "Response: $response"
print_status $? "Check AI companion attention"

# Test 4: Get AI companion's activity status
echo -e "\n${BLUE}Test 4: Get AI Companion's Activity Status${NC}"
response=$(curl -s -X GET "$BASE_URL/activity")
echo "Response: $response"
print_status $? "Get AI companion activity"

# Test 5: Interact with AI companion - Chat
echo -e "\n${BLUE}Test 5: Interact with AI Companion - Chat${NC}"
response=$(curl -s -X POST "$BASE_URL/interact" \
    -H "Content-Type: application/json" \
    -d '{
        "type": "chat",
        "message": "Hello, how are you today?"
    }')
echo "Response: $response"
print_status $? "AI companion interaction - chat"

# Test 6: Interact with AI companion - Command/Help
echo -e "\n${BLUE}Test 6: Interact with AI Companion - Command/Help${NC}"
response=$(curl -s -X POST "$BASE_URL/interact" \
    -H "Content-Type: application/json" \
    -d '{
        "type": "command",
        "message": "Can you help me with something?"
    }')
echo "Response: $response"
print_status $? "AI companion interaction - command"

# Test 7: Interact with AI companion - Question
echo -e "\n${BLUE}Test 7: Interact with AI Companion - Question${NC}"
response=$(curl -s -X POST "$BASE_URL/interact" \
    -H "Content-Type: application/json" \
    -d '{
        "type": "question",
        "message": "What is the weather like today?"
    }')
echo "Response: $response"
print_status $? "AI companion interaction - question"

# Test 8: Interact with AI companion - Greet
echo -e "\n${BLUE}Test 8: Interact with AI Companion - Greet${NC}"
response=$(curl -s -X POST "$BASE_URL/interact" \
    -H "Content-Type: application/json" \
    -d '{
        "type": "greet",
        "message": "Good morning!"
    }')
echo "Response: $response"
print_status $? "AI companion interaction - greet"

# Test 9: Put AI companion to sleep
echo -e "\n${BLUE}Test 9: Put AI Companion to Sleep${NC}"
response=$(curl -s -X POST "$BASE_URL/sleep")
echo "Response: $response"
print_status $? "Put AI companion to sleep"

# Test 10: Wake up AI companion
echo -e "\n${BLUE}Test 10: Wake Up AI Companion${NC}"
response=$(curl -s -X POST "$BASE_URL/wake")
echo "Response: $response"
print_status $? "Wake up AI companion"

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

echo -e "\n${YELLOW}🎉 AI Companion API Testing Complete!${NC}"
echo -e "${YELLOW}The AI companion should now show different emotional states, LED colors, and sounds based on interactions.${NC}" 