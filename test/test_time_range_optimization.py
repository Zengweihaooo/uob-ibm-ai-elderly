#!/usr/bin/env python3
"""
Time Range Optimization Test Script
Tests the health statistics API with different time periods
"""

import requests
import time
import json
from datetime import datetime, timedelta

# Configuration
BASE_URL = "http://localhost:8080"
AUTH_HEADER = "Authorization: Bearer test-token"

def test_health_record_creation():
    """Test creating health records for today"""
    print("=== 测试创建健康记录 ===")
    
    # Create some test health records for today
    test_records = [
        {"type": "bloodPressure", "value": "120/80"},
        {"type": "bloodSugar", "value": "120"},
        {"type": "steps", "value": "8000"},
        {"type": "bloodPressure", "value": "125/85"},
        {"type": "bloodSugar", "value": "110"}
    ]
    
    created_records = []
    
    for record in test_records:
        try:
            response = requests.post(
                f"{BASE_URL}/api/health/record",
                headers={
                    "Content-Type": "application/json",
                    AUTH_HEADER
                },
                json=record,
                timeout=10
            )
            
            if response.status_code == 200:
                result = response.json()
                if result.get("success"):
                    print(f"✅ 创建记录成功: {record['type']} = {record['value']}")
                    created_records.append(result.get("record"))
                else:
                    print(f"❌ 创建记录失败: {result.get('message')}")
            else:
                print(f"❌ HTTP错误: {response.status_code}")
                
        except Exception as e:
            print(f"❌ 请求错误: {e}")
    
    return created_records

def test_today_records_api():
    """Test the /api/health/today endpoint"""
    print("\n=== 测试今日健康记录API ===")
    
    try:
        response = requests.get(
            f"{BASE_URL}/api/health/today",
            headers={AUTH_HEADER},
            timeout=10
        )
        
        if response.status_code == 200:
            result = response.json()
            if result.get("success"):
                records = result.get("records", [])
                print(f"✅ 今日记录API成功，返回 {len(records)} 条记录")
                
                for record in records:
                    print(f"   - {record.get('type')}: {record.get('value')} (时间: {record.get('recordTime')})")
                
                return records
            else:
                print(f"❌ API返回错误: {result.get('message')}")
        else:
            print(f"❌ HTTP错误: {response.status_code}")
            
    except Exception as e:
        print(f"❌ 请求错误: {e}")
    
    return []

def test_statistics_api(period="today"):
    """Test the /api/health/statistics endpoint"""
    print(f"\n=== 测试健康统计API (period={period}) ===")
    
    try:
        response = requests.get(
            f"{BASE_URL}/api/health/statistics",
            headers={AUTH_HEADER},
            params={"period": period},
            timeout=10
        )
        
        if response.status_code == 200:
            result = response.json()
            if result.get("success"):
                print(f"✅ 统计API成功 (period={period})")
                print(f"   总记录数: {result.get('totalRecords', 0)}")
                print(f"   异常记录数: {result.get('abnormalRecords', 0)}")
                print(f"   正常记录数: {result.get('normalRecords', 0)}")
                print(f"   异常率: {result.get('abnormalRate', 0):.2%}")
                
                type_count = result.get('typeCount', {})
                if type_count:
                    print("   按类型统计:")
                    for type_name, count in type_count.items():
                        print(f"     - {type_name}: {count}")
                
                return result
            else:
                print(f"❌ API返回错误: {result.get('message')}")
        else:
            print(f"❌ HTTP错误: {response.status_code}")
            
    except Exception as e:
        print(f"❌ 请求错误: {e}")
    
    return None

def test_different_periods():
    """Test statistics API with different periods"""
    print("\n=== 测试不同时间段的统计 ===")
    
    periods = ["today", "week", "month"]
    
    for period in periods:
        result = test_statistics_api(period)
        if result:
            print(f"   {period.upper()} 统计完成")
        else:
            print(f"   {period.upper()} 统计失败")
        time.sleep(1)  # 避免请求过快

def check_database_records():
    """Check what records exist in the database"""
    print("\n=== 检查数据库中的记录 ===")
    
    try:
        # Get all records
        response = requests.get(
            f"{BASE_URL}/api/health/records",
            headers={AUTH_HEADER},
            timeout=10
        )
        
        if response.status_code == 200:
            result = response.json()
            if result.get("success"):
                records = result.get("records", [])
                print(f"✅ 数据库中共有 {len(records)} 条记录")
                
                if records:
                    print("   最近的记录:")
                    for i, record in enumerate(records[:5]):  # 显示最近5条
                        print(f"     {i+1}. {record.get('type')}: {record.get('value')} (时间: {record.get('recordTime')})")
                
                return records
            else:
                print(f"❌ API返回错误: {result.get('message')}")
        else:
            print(f"❌ HTTP错误: {response.status_code}")
            
    except Exception as e:
        print(f"❌ 请求错误: {e}")
    
    return []

def main():
    """Run all tests"""
    print("=" * 60)
    print("时间范围优化测试")
    print("=" * 60)
    print(f"测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"目标URL: {BASE_URL}")
    print()
    
    # Step 1: Check existing records
    existing_records = check_database_records()
    
    # Step 2: Create test records if needed
    if len(existing_records) < 3:
        print("\n创建测试记录...")
        created_records = test_health_record_creation()
        time.sleep(2)  # Wait for records to be saved
    else:
        print(f"\n已有 {len(existing_records)} 条记录，跳过创建")
    
    # Step 3: Test today records API
    today_records = test_today_records_api()
    
    # Step 4: Test statistics API
    today_stats = test_statistics_api("today")
    
    # Step 5: Test different periods
    test_different_periods()
    
    # Step 6: Summary
    print("\n" + "=" * 60)
    print("测试总结")
    print("=" * 60)
    
    if today_records:
        print(f"✅ 今日记录API: 成功获取 {len(today_records)} 条记录")
    else:
        print("❌ 今日记录API: 失败")
    
    if today_stats and today_stats.get('totalRecords', 0) > 0:
        print(f"✅ 今日统计API: 成功，总记录数 {today_stats.get('totalRecords')}")
    else:
        print("❌ 今日统计API: 失败或无数据")
    
    print("\n时间范围优化测试完成！")

if __name__ == "__main__":
    main()
