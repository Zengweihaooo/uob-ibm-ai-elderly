#!/bin/bash

# Test script for Pet Emotion API
# This script tests the pet emotion expression functionality

BASE_URL="http://localhost:8080/api/pet-emotion"

echo "🐾 Testing Pet Emotion API"
echo "=========================="

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

# Test 1: Get pet's emotional state
echo -e "\n${BLUE}Test 1: Get Pet's Emotional State${NC}"
response=$(curl -s -X GET "$BASE_URL/state")
echo "Response: $response" | jq '.'
print_status $? "Get pet emotional state"

# Test 2: Get pet's expressions (sound and visual)
echo -e "\n${BLUE}Test 2: Get Pet's Expressions${NC}"
response=$(curl -s -X GET "$BASE_URL/expressions")
echo "Response: $response" | jq '.'
print_status $? "Get pet expressions"

# Test 3: Check if pet needs attention
echo -e "\n${BLUE}Test 3: Check Pet Attention Status${NC}"
response=$(curl -s -X GET "$BASE_URL/attention-check")
echo "Response: $response" | jq '.'
print_status $? "Check pet attention"

# Test 4: Get pet's movement status
echo -e "\n${BLUE}Test 4: Get Pet's Movement Status${NC}"
response=$(curl -s -X GET "$BASE_URL/movement")
echo "Response: $response" | jq '.'
print_status $? "Get pet movement"

# Test 5: Interact with pet - Pet/Stroke
echo -e "\n${BLUE}Test 5: Interact with Pet - Pet/Stroke${NC}"
response=$(curl -s -X POST "$BASE_URL/interact" \
    -H "Content-Type: application/json" \
    -d '{
        "type": "pet",
        "message": "Good boy!"
    }')
echo "Response: $response" | jq '.'
print_status $? "Pet interaction - stroke"

# Test 6: Interact with pet - Play
echo -e "\n${BLUE}Test 6: Interact with Pet - Play${NC}"
response=$(curl -s -X POST "$BASE_URL/interact" \
    -H "Content-Type: application/json" \
    -d '{
        "type": "play",
        "message": "Let\'s play fetch!"
    }')
echo "Response: $response" | jq '.'
print_status $? "Pet interaction - play"

# Test 7: Interact with pet - Feed
echo -e "\n${BLUE}Test 7: Interact with Pet - Feed${NC}"
response=$(curl -s -X POST "$BASE_URL/interact" \
    -H "Content-Type: application/json" \
    -d '{
        "type": "feed",
        "message": "Here\'s your dinner!"
    }')
echo "Response: $response" | jq '.'
print_status $? "Pet interaction - feed"

# Test 8: Interact with pet - Talk
echo -e "\n${BLUE}Test 8: Interact with Pet - Talk${NC}"
response=$(curl -s -X POST "$BASE_URL/interact" \
    -H "Content-Type: application/json" \
    -d '{
        "type": "talk",
        "message": "How are you doing today?"
    }')
echo "Response: $response" | jq '.'
print_status $? "Pet interaction - talk"

# Test 9: Make pet stay in place
echo -e "\n${BLUE}Test 9: Make Pet Stay in Place${NC}"
response=$(curl -s -X POST "$BASE_URL/stay")
echo "Response: $response" | jq '.'
print_status $? "Make pet stay"

# Test 10: Check emotional state after interactions
echo -e "\n${BLUE}Test 10: Check Emotional State After Interactions${NC}"
response=$(curl -s -X GET "$BASE_URL/state")
echo "Response: $response" | jq '.'
print_status $? "Check emotional state after interactions"

# Test 11: Check expressions after interactions
echo -e "\n${BLUE}Test 11: Check Expressions After Interactions${NC}"
response=$(curl -s -X GET "$BASE_URL/expressions")
echo "Response: $response" | jq '.'
print_status $? "Check expressions after interactions"

echo -e "\n${YELLOW}🎉 Pet Emotion API Testing Complete!${NC}"
echo -e "${YELLOW}The pet should now show different emotional states, sounds, and visual expressions based on interactions.${NC}" 