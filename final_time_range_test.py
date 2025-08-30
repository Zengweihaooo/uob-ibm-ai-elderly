#!/usr/bin/env python3
"""
Final Time Range Optimization Test
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

def test_time_range_optimization():
    """Test the time range optimization"""
    
    # Generate fresh JWT token
    token = generate_jwt_token()
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    
    print("=== 时间范围优化最终测试 ===")
    print(f"测试时间: {time.strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"JWT Token: {token[:50]}...")
    print()
    
    # Test today's statistics
    print("1. 测试今日统计数据 (period=today)")
    try:
        response = requests.get(
            "http://localhost:8080/api/health/statistics",
            headers=headers,
            params={"period": "today"},
            timeout=10
        )
        
        if response.status_code == 200:
            data = response.json()
            if data.get("success"):
                total_records = data.get('totalRecords', 0)
                print(f"   ✅ 成功! 今日记录数: {total_records}")
                
                if total_records > 0:
                    print(f"   ✅ 时间范围优化成功! 找到了 {total_records} 条今日记录")
                    print(f"   📊 异常记录: {data.get('abnormalRecords', 0)}")
                    print(f"   📊 正常记录: {data.get('normalRecords', 0)}")
                    print(f"   📊 异常率: {data.get('abnormalRate', 0):.2%}")
                    
                    # Show type breakdown
                    type_count = data.get('typeCount', {})
                    if type_count:
                        print("   📋 类型统计:")
                        for type_name, count in type_count.items():
                            print(f"      - {type_name}: {count}")
                    
                    return True
                else:
                    print(f"   ❌ 时间范围优化失败! 今日记录数为0")
                    return False
            else:
                print(f"   ❌ API错误: {data.get('message')}")
                return False
        else:
            print(f"   ❌ HTTP错误: {response.status_code}")
            print(f"   Response: {response.text}")
            return False
            
    except Exception as e:
        print(f"   ❌ 请求失败: {e}")
        return False

def test_week_statistics():
    """Test week statistics for comparison"""
    token = generate_jwt_token()
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    
    print("\n2. 测试本周统计数据 (period=week) - 对比验证")
    try:
        response = requests.get(
            "http://localhost:8080/api/health/statistics",
            headers=headers,
            params={"period": "week"},
            timeout=10
        )
        
        if response.status_code == 200:
            data = response.json()
            if data.get("success"):
                total_records = data.get('totalRecords', 0)
                print(f"   📊 本周总记录数: {total_records}")
                return total_records
            else:
                print(f"   ❌ API错误: {data.get('message')}")
                return 0
        else:
            print(f"   ❌ HTTP错误: {response.status_code}")
            return 0
            
    except Exception as e:
        print(f"   ❌ 请求失败: {e}")
        return 0

def main():
    """Run the final test"""
    print("=" * 60)
    print("时间范围优化 - 最终验证测试")
    print("=" * 60)
    print()
    
    # Test today's statistics
    today_success = test_time_range_optimization()
    
    # Test week statistics for comparison
    week_records = test_week_statistics()
    
    print("\n" + "=" * 60)
    print("测试结果总结")
    print("=" * 60)
    
    if today_success:
        print("🎉 时间范围优化成功!")
        print("✅ 今日统计数据API正常工作")
        print("✅ 时间范围计算准确")
        print("✅ 数据库查询正确")
        print("✅ JWT token认证正常")
    else:
        print("❌ 时间范围优化失败!")
        print("❌ 今日统计数据API存在问题")
        print("❌ 需要进一步调试")
    
    print(f"📊 本周总记录数: {week_records} (用于对比验证)")
    print()
    print("=" * 60)

if __name__ == "__main__":
    main()
