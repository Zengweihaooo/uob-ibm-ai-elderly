#!/usr/bin/env python3
"""
Health Record Time Fix Test
测试健康记录时间修复效果
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

def test_health_time_fix():
    """Test health record time fix"""
    
    # Generate fresh JWT token
    token = generate_jwt_token()
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    
    print("=== 健康记录时间修复测试 ===")
    print(f"Token: {token[:50]}...")
    
    # 1. 添加新的健康记录
    print("\n1. 添加新的健康记录...")
    new_record_data = {
        "type": "bloodPressure",
        "value": "118/75"
    }
    
    response = requests.post(
        "http://localhost:8080/api/health/record",
        headers=headers,
        json=new_record_data
    )
    
    if response.status_code == 200:
        result = response.json()
        print(f"✅ 记录添加成功: {result.get('success')}")
        if 'record' in result:
            record = result['record']
            print(f"   记录时间: {record.get('recordTime')}")
            print(f"   创建时间: {record.get('createdAt')}")
    else:
        print(f"❌ 记录添加失败: {response.status_code}")
        print(response.text)
    
    # 2. 获取所有健康记录
    print("\n2. 获取所有健康记录...")
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
            for i, record in enumerate(records[:3]):  # 只显示前3条
                print(f"   记录 {i+1}:")
                print(f"     ID: {record.get('id')}")
                print(f"     类型: {record.get('type')}")
                print(f"     值: {record.get('value')}")
                print(f"     记录时间: {record.get('recordTime')}")
                print(f"     创建时间: {record.get('createdAt')}")
                print(f"     更新时间: {record.get('updatedAt')}")
    else:
        print(f"❌ 获取记录失败: {response.status_code}")
        print(response.text)
    
    # 3. 获取今日记录
    print("\n3. 获取今日记录...")
    response = requests.get(
        "http://localhost:8080/api/health/today",
        headers=headers
    )
    
    if response.status_code == 200:
        result = response.json()
        print(f"✅ 获取今日记录成功: {result.get('success')}")
        if 'records' in result:
            records = result['records']
            print(f"   今日记录数: {len(records)}")
            for record in records:
                print(f"     {record.get('type')}: {record.get('value')} - {record.get('recordTime')}")
    else:
        print(f"❌ 获取今日记录失败: {response.status_code}")
        print(response.text)
    
    print("\n=== 测试完成 ===")

if __name__ == "__main__":
    test_health_time_fix()
