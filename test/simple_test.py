#!/usr/bin/env python3
import requests
import json

# Test the statistics API
url = "http://localhost:8080/api/health/statistics"
headers = {"Authorization": "Bearer user-token-1-1234567890"}

print("Testing health statistics API...")

try:
    response = requests.get(url, headers=headers, params={"period": "today"})
    print(f"Status: {response.status_code}")
    print(f"Response: {response.text}")
    
    if response.status_code == 200:
        data = response.json()
        if data.get("success"):
            print("✅ API call successful!")
            print(f"Total records: {data.get('totalRecords', 0)}")
            print(f"Period: {data.get('period', 'unknown')}")
        else:
            print(f"❌ API returned error: {data.get('message')}")
    else:
        print(f"❌ HTTP error: {response.status_code}")
        
except Exception as e:
    print(f"❌ Request failed: {e}")
