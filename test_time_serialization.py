#!/usr/bin/env python3
"""
Time Serialization Test
测试时间序列化问题
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

def test_time_serialization():
    """Test time serialization issue"""
    
    # Generate fresh JWT token
    token = generate_jwt_token()
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    
    print("=== 时间序列化测试 ===")
    
    # 获取所有健康记录
    print("\n获取所有健康记录...")
    response = requests.get(
        "http://localhost:8080/api/health/records",
        headers=headers
    )
    
    if response.status_code == 200:
        result = response.json()
        print(f"✅ 获取记录成功: {result.get('success')}")
        if 'records' in result:
            records = result['records']
            print(f"   总记录数: {len(records)}")
            
            # 检查第一条记录的所有字段
            if records:
                first_record = records[0]
                print(f"\n第一条记录的原始JSON:")
                print(json.dumps(first_record, indent=2, default=str))
                
                print(f"\n时间字段分析:")
                print(f"   recordTime: {first_record.get('recordTime')} (type: {type(first_record.get('recordTime'))})")
                print(f"   createdAt: {first_record.get('createdAt')} (type: {type(first_record.get('createdAt'))})")
                print(f"   updatedAt: {first_record.get('updatedAt')} (type: {type(first_record.get('updatedAt'))})")
                
                # 检查是否为None
                if first_record.get('recordTime') is None:
                    print("❌ recordTime 是 None")
                else:
                    print("✅ recordTime 不是 None")
                    
                if first_record.get('createdAt') is None:
                    print("❌ createdAt 是 None")
                else:
                    print("✅ createdAt 不是 None")
    else:
        print(f"❌ 获取记录失败: {response.status_code}")
        print(response.text)
    
    print("\n=== 测试完成 ===")

if __name__ == "__main__":
    test_time_serialization()
