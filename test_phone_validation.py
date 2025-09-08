#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试手机号验证功能
"""

import requests
import json

# 配置
BASE_URL = "http://localhost:8080"
API_ENDPOINT = f"{BASE_URL}/api/family/contacts"

def test_phone_validation():
    """测试各种手机号格式"""
    
    # 测试用例：[手机号, 是否应该通过验证, 描述]
    test_cases = [
        # 有效的中国手机号
        ("13800138000", True, "标准中国手机号"),
        ("+8613800138000", True, "带国家代码的中国手机号"),
        ("138 0013 8000", True, "带空格的中国手机号"),
        ("15912345678", True, "另一个有效的中国手机号"),
        
        # 无效的重复数字
        ("11111111111", False, "11个1的重复数字"),
        ("111111", False, "6个1的重复数字"),
        ("+8611111111111", False, "带国家代码的重复数字"),
        ("88888888888", False, "11个8的重复数字"),
        
        # 无效的连续数字
        ("12345678901", False, "连续递增数字"),
        ("98765432101", False, "连续递减数字"),
        ("+8612345678901", False, "带国家代码的连续数字"),
        
        # 无效的号段
        ("12012345678", False, "无效的中国手机号段120"),
        ("11012345678", False, "无效的中国手机号段110"),
        ("10012345678", False, "无效的中国手机号段100"),
        
        # 长度不正确
        ("138001", False, "号码太短"),
        ("1380013800012345", False, "号码太长"),
        
        # 国际号码
        ("+1234567890", True, "美国号码格式"),
        ("+44123456789", True, "英国号码格式"),
        
        # 无效的国际号码
        ("+1111111111", False, "重复数字的国际号码"),
        ("+1234567890123456", False, "太长的国际号码"),
        ("+123", False, "太短的国际号码"),
        
        # 边界情况
        ("", False, "空字符串"),
        (None, False, "None值"),
        ("abc123", False, "包含字母"),
        ("138-0013-8000", True, "带连字符的号码（会被清理）"),
    ]
    
    print("🧪 开始测试手机号验证功能")
    print("=" * 60)
    
    passed = 0
    failed = 0
    
    # 模拟JWT token（实际使用时需要真实的token）
    headers = {
        "Content-Type": "application/json",
        "Authorization": "Bearer test-token"  # 需要替换为真实token
    }
    
    for phone, expected, description in test_cases:
        try:
            # 构造请求数据
            contact_data = {
                "name": "测试联系人",
                "phone": phone,
                "relationship": "friend"
            }
            
            # 发送请求
            response = requests.post(API_ENDPOINT, 
                                   json=contact_data, 
                                   headers=headers,
                                   timeout=10)
            
            # 分析结果
            success = response.status_code == 200
            
            if success == expected:
                status = "✅ 通过"
                passed += 1
            else:
                status = "❌ 失败"
                failed += 1
            
            print(f"{status} | {description}")
            print(f"   手机号: {phone}")
            print(f"   期望: {'通过' if expected else '失败'}, 实际: {'通过' if success else '失败'}")
            
            if response.status_code != 200:
                try:
                    error_info = response.json()
                    print(f"   错误信息: {error_info.get('message', '未知错误')}")
                except:
                    print(f"   HTTP状态码: {response.status_code}")
            
            print()
            
        except requests.exceptions.RequestException as e:
            print(f"❌ 网络错误 | {description}")
            print(f"   手机号: {phone}")
            print(f"   错误: {str(e)}")
            print()
            failed += 1
        except Exception as e:
            print(f"❌ 未知错误 | {description}")
            print(f"   手机号: {phone}")
            print(f"   错误: {str(e)}")
            print()
            failed += 1
    
    # 输出总结
    print("=" * 60)
    print(f"📊 测试总结:")
    print(f"   总计: {passed + failed} 个测试用例")
    print(f"   通过: {passed} 个")
    print(f"   失败: {failed} 个")
    print(f"   成功率: {(passed / (passed + failed) * 100):.1f}%")
    
    if failed == 0:
        print("🎉 所有测试都通过了！")
    else:
        print(f"⚠️  有 {failed} 个测试失败，请检查验证逻辑")

if __name__ == "__main__":
    print("📱 手机号验证测试工具")
    print("请确保Spring Boot应用正在运行在 http://localhost:8080")
    print()
    
    try:
        # 简单的连接测试
        response = requests.get(f"{BASE_URL}/actuator/health", timeout=5)
        if response.status_code == 200:
            print("✅ 服务器连接正常")
        else:
            print("⚠️  服务器响应异常，但继续测试")
    except:
        print("❌ 无法连接到服务器，请检查服务是否启动")
        print("   继续测试（可能会失败）...")
    
    print()
    test_phone_validation()
