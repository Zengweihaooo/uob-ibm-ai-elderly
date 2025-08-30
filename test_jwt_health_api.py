#!/usr/bin/env python3
"""
Test Health Statistics API with JWT Token
"""

import requests
import json
import jwt
import time

def generate_jwt_token(user_id=1, email="test@example.com"):
    """Generate a test JWT token"""
    secret = "yourSuperSecretKeyForJWTTokenGenerationChangeInProduction"
    
    payload = {
        "userId": user_id,
        "email": email,
        "iat": int(time.time()),
        "exp": int(time.time()) + 86400,
        "sub": email
    }
    
    return jwt.encode(payload, secret, algorithm="HS256")

def test_health_statistics_api():
    """Test the health statistics API with JWT token"""
    
    # Generate JWT token
    token = generate_jwt_token()
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    
    print("=== Testing Health Statistics API with JWT Token ===")
    print(f"JWT Token: {token[:50]}...")
    print()
    
    # Test different periods
    periods = ["today", "week", "month"]
    
    for period in periods:
        print(f"Testing period: {period}")
        
        try:
            response = requests.get(
                "http://localhost:8080/api/health/statistics",
                headers=headers,
                params={"period": period},
                timeout=10
            )
            
            print(f"Status: {response.status_code}")
            
            if response.status_code == 200:
                data = response.json()
                if data.get("success"):
                    print("✅ API call successful!")
                    print(f"   Period: {data.get('period')}")
                    print(f"   Total records: {data.get('totalRecords', 0)}")
                    print(f"   Abnormal records: {data.get('abnormalRecords', 0)}")
                    print(f"   Normal records: {data.get('normalRecords', 0)}")
                    print(f"   Abnormal rate: {data.get('abnormalRate', 0):.2%}")
                    
                    # Show type breakdown
                    type_count = data.get('typeCount', {})
                    if type_count:
                        print("   Type breakdown:")
                        for type_name, count in type_count.items():
                            print(f"     - {type_name}: {count}")
                else:
                    print(f"❌ API returned error: {data.get('message')}")
            else:
                print(f"❌ HTTP error: {response.status_code}")
                print(f"Response: {response.text}")
                
        except Exception as e:
            print(f"❌ Request failed: {e}")
        
        print()

def test_health_record_creation():
    """Test creating a health record with JWT token"""
    
    token = generate_jwt_token()
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    
    print("=== Testing Health Record Creation ===")
    
    test_record = {
        "type": "bloodPressure",
        "value": "130/85"
    }
    
    try:
        response = requests.post(
            "http://localhost:8080/api/health/record",
            headers=headers,
            json=test_record,
            timeout=10
        )
        
        print(f"Status: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            if data.get("success"):
                print("✅ Record created successfully!")
                print(f"   Record ID: {data.get('record', {}).get('id')}")
                print(f"   Type: {data.get('record', {}).get('type')}")
                print(f"   Value: {data.get('record', {}).get('value')}")
                print(f"   Abnormal: {data.get('abnormal', False)}")
            else:
                print(f"❌ API returned error: {data.get('message')}")
        else:
            print(f"❌ HTTP error: {response.status_code}")
            print(f"Response: {response.text}")
            
    except Exception as e:
        print(f"❌ Request failed: {e}")

def main():
    """Run all tests"""
    print("=" * 60)
    print("Health API JWT Token Testing")
    print("=" * 60)
    print()
    
    # Test record creation first
    test_health_record_creation()
    print()
    
    # Test statistics API
    test_health_statistics_api()
    
    print("=" * 60)
    print("Testing completed!")
    print("=" * 60)

if __name__ == "__main__":
    main()
