#!/usr/bin/env python3
"""
JWT Token Generator for Testing
Generates valid JWT tokens for testing the health statistics API
"""

import jwt
import time
from datetime import datetime, timedelta

def generate_test_jwt_token(user_id=1, email="test@example.com"):
    """Generate a test JWT token"""
    
    # JWT secret key (must match the one in application.properties)
    secret = "yourSuperSecretKeyForJWTTokenGenerationChangeInProduction"
    
    # Token payload
    payload = {
        "userId": user_id,
        "email": email,
        "iat": int(time.time()),  # issued at
        "exp": int(time.time()) + 86400,  # expires in 24 hours
        "sub": email  # subject
    }
    
    # Generate JWT token
    token = jwt.encode(payload, secret, algorithm="HS256")
    
    return token

def test_jwt_token():
    """Test the generated JWT token"""
    token = generate_test_jwt_token()
    
    print("=== JWT Token Generator ===")
    print(f"Generated token: {token}")
    print(f"Token length: {len(token)} characters")
    print(f"Contains dots: {token.count('.')}")
    
    # Decode to verify
    try:
        decoded = jwt.decode(token, "yourSuperSecretKeyForJWTTokenGenerationChangeInProduction", algorithms=["HS256"])
        print(f"Decoded payload: {decoded}")
        print("✅ Token generated successfully!")
        return token
    except Exception as e:
        print(f"❌ Token validation failed: {e}")
        return None

if __name__ == "__main__":
    token = test_jwt_token()
    if token:
        print(f"\nUse this token for testing:")
        print(f"Authorization: Bearer {token}")
