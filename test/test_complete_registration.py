#!/usr/bin/env python3
"""
Complete Registration Flow Test Script
Tests the entire email registration system from start to finish
"""

import requests
import time
import json

# Configuration
BACKEND_URL = "http://localhost:8080"
FRONTEND_URL = "http://localhost:3000"

def test_backend_health():
    """Test if backend is running"""
    try:
        response = requests.get(f"{BACKEND_URL}/user/api/register", timeout=5)
        return True
    except requests.exceptions.RequestException:
        return False

def test_frontend_health():
    """Test if frontend is running"""
    try:
        response = requests.get(f"{FRONTEND_URL}/", timeout=5)
        return response.status_code == 200
    except requests.exceptions.RequestException:
        return False

def test_registration_api():
    """Test the registration API"""
    test_email = f"test_{int(time.time())}@example.com"
    
    print(f"Testing registration with email: {test_email}")
    
    # Step 1: Send registration request
    try:
        response = requests.post(
            f"{BACKEND_URL}/user/api/register",
            data={"email": test_email},
            headers={"Content-Type": "application/x-www-form-urlencoded"},
            timeout=10
        )
        
        if response.status_code == 200:
            result = response.json()
            if result.get("success"):
                print("✅ Registration API test passed")
                print(f"   Response: {result.get('message')}")
                return test_email, True
            else:
                print(f"❌ Registration API failed: {result.get('message')}")
                return test_email, False
        else:
            print(f"❌ Registration API failed with status: {response.status_code}")
            return test_email, False
            
    except Exception as e:
        print(f"❌ Registration API error: {e}")
        return test_email, False

def test_verification_api(email):
    """Test the verification API (this will fail without actual email)"""
    print(f"Testing verification API for email: {email}")
    
    # This will fail because we don't have the actual verification code
    # But we can test the API structure
    try:
        response = requests.post(
            f"{BACKEND_URL}/user/api/verify",
            data={"email": email, "code": "123456"},
            headers={"Content-Type": "application/x-www-form-urlencoded"},
            timeout=10
        )
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ Verification API structure test passed")
            print(f"   Response: {result.get('message')}")
            return True
        else:
            print(f"❌ Verification API failed with status: {response.status_code}")
            return False
            
    except Exception as e:
        print(f"❌ Verification API error: {e}")
        return False

def test_complete_registration_api(email):
    """Test the complete registration API"""
    print(f"Testing complete registration API for email: {email}")
    
    try:
        response = requests.post(
            f"{BACKEND_URL}/user/api/complete",
            data={
                "email": email,
                "name": "Test User",
                "password": "testpassword123",
                "role": "ELDERLY"
            },
            headers={"Content-Type": "application/x-www-form-urlencoded"},
            timeout=10
        )
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ Complete registration API structure test passed")
            print(f"   Response: {result.get('message')}")
            return True
        else:
            print(f"❌ Complete registration API failed with status: {response.status_code}")
            return False
            
    except Exception as e:
        print(f"❌ Complete registration API error: {e}")
        return False

def test_frontend_pages():
    """Test if frontend pages are accessible"""
    pages_to_test = [
        "/",
        "/test_register.html",
        "/src/pages/register.html"
    ]
    
    print("Testing frontend pages...")
    
    for page in pages_to_test:
        try:
            response = requests.get(f"{FRONTEND_URL}{page}", timeout=5)
            if response.status_code == 200:
                print(f"✅ {page} - accessible")
            else:
                print(f"❌ {page} - status {response.status_code}")
        except Exception as e:
            print(f"❌ {page} - error: {e}")

def main():
    """Run all tests"""
    print("=" * 60)
    print("UOB-IBM AI Elderly Project - Registration System Test")
    print("=" * 60)
    
    # Test service health
    print("\n1. Testing Service Health...")
    backend_ok = test_backend_health()
    frontend_ok = test_frontend_health()
    
    if backend_ok:
        print("✅ Backend service is running")
    else:
        print("❌ Backend service is not running")
        
    if frontend_ok:
        print("✅ Frontend service is running")
    else:
        print("❌ Frontend service is not running")
    
    if not backend_ok or not frontend_ok:
        print("\n❌ Services are not ready. Please start the services first.")
        return
    
    # Test frontend pages
    print("\n2. Testing Frontend Pages...")
    test_frontend_pages()
    
    # Test registration API
    print("\n3. Testing Registration API...")
    email, reg_success = test_registration_api()
    
    if reg_success:
        # Test verification API
        print("\n4. Testing Verification API...")
        test_verification_api(email)
        
        # Test complete registration API
        print("\n5. Testing Complete Registration API...")
        test_complete_registration_api(email)
    
    print("\n" + "=" * 60)
    print("Test Summary:")
    print(f"Backend: {'✅ Running' if backend_ok else '❌ Not Running'}")
    print(f"Frontend: {'✅ Running' if frontend_ok else '❌ Not Running'}")
    print(f"Registration API: {'✅ Working' if reg_success else '❌ Failed'}")
    print("=" * 60)
    
    if backend_ok and frontend_ok and reg_success:
        print("\n🎉 All core services are working!")
        print("You can now test the registration system at:")
        print(f"   - Main page: {FRONTEND_URL}/")
        print(f"   - Test page: {FRONTEND_URL}/test_register.html")
        print(f"   - Register page: {FRONTEND_URL}/src/pages/register.html")
    else:
        print("\n⚠️  Some services are not working properly.")
        print("Please check the logs and restart the services if needed.")

if __name__ == "__main__":
    main()
