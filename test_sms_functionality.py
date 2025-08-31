#!/usr/bin/env python3
"""
SMS功能测试脚本
测试Twilio SMS集成功能

使用方法：
python test_sms_functionality.py

作者：AI Assistant
版本：1.0
"""

import requests
import json
import time

# 配置
BASE_URL = "http://localhost:8080"
SMS_API_URL = f"{BASE_URL}/api/sms"

def test_send_sms():
    """测试发送SMS功能"""
    print("=" * 50)
    print("📱 测试SMS发送功能")
    print("=" * 50)
    
    # 测试数据
    test_data = {
        "phoneNumber": "+8613800138000",  # 中国测试号码
        "message": "这是一条测试短信，来自IBM AI老年人助手系统！",
        "messageType": "TEST"
    }
    
    try:
        print(f"发送POST请求到: {SMS_API_URL}/send")
        print(f"请求数据: {json.dumps(test_data, indent=2, ensure_ascii=False)}")
        
        response = requests.post(
            f"{SMS_API_URL}/send",
            json=test_data,
            headers={"Content-Type": "application/json"},
            timeout=30
        )
        
        print(f"响应状态码: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            print("✅ SMS发送成功！")
            print(f"响应数据: {json.dumps(result, indent=2, ensure_ascii=False)}")
        else:
            print(f"❌ SMS发送失败，状态码: {response.status_code}")
            print(f"错误信息: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print("❌ 连接失败！请确保Spring Boot应用正在运行在端口8080")
    except Exception as e:
        print(f"❌ 测试失败: {str(e)}")

def test_health_alert_sms():
    """测试健康警报SMS功能"""
    print("=" * 50)
    print("🏥 测试健康警报SMS功能")
    print("=" * 50)
    
    # 测试数据
    test_data = {
        "phoneNumber": "+8613800138000",
        "healthData": "血压异常：160/95 mmHg，心率：95 BPM"
    }
    
    try:
        print(f"发送POST请求到: {SMS_API_URL}/health-alert")
        print(f"请求数据: {json.dumps(test_data, indent=2, ensure_ascii=False)}")
        
        response = requests.post(
            f"{SMS_API_URL}/health-alert",
            json=test_data,
            headers={"Content-Type": "application/json"},
            timeout=30
        )
        
        print(f"响应状态码: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            print("✅ 健康警报SMS发送成功！")
            print(f"响应数据: {json.dumps(result, indent=2, ensure_ascii=False)}")
        else:
            print(f"❌ 健康警报SMS发送失败，状态码: {response.status_code}")
            print(f"错误信息: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print("❌ 连接失败！请确保Spring Boot应用正在运行在端口8080")
    except Exception as e:
        print(f"❌ 测试失败: {str(e)}")

def test_emergency_sms():
    """测试紧急SMS功能"""
    print("=" * 50)
    print("🚨 测试紧急SMS功能")
    print("=" * 50)
    
    # 测试数据
    test_data = {
        "phoneNumber": "+8613800138000",
        "emergencyInfo": "用户摔倒检测警报！位置：家中客厅，时间：" + time.strftime("%Y-%m-%d %H:%M:%S")
    }
    
    try:
        print(f"发送POST请求到: {SMS_API_URL}/emergency")
        print(f"请求数据: {json.dumps(test_data, indent=2, ensure_ascii=False)}")
        
        response = requests.post(
            f"{SMS_API_URL}/emergency",
            json=test_data,
            headers={"Content-Type": "application/json"},
            timeout=30
        )
        
        print(f"响应状态码: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            print("✅ 紧急SMS发送成功！")
            print(f"响应数据: {json.dumps(result, indent=2, ensure_ascii=False)}")
        else:
            print(f"❌ 紧急SMS发送失败，状态码: {response.status_code}")
            print(f"错误信息: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print("❌ 连接失败！请确保Spring Boot应用正在运行在端口8080")
    except Exception as e:
        print(f"❌ 测试失败: {str(e)}")

def test_sms_history():
    """测试SMS历史记录功能"""
    print("=" * 50)
    print("📋 测试SMS历史记录功能")
    print("=" * 50)
    
    try:
        print(f"发送GET请求到: {SMS_API_URL}/history")
        
        response = requests.get(
            f"{SMS_API_URL}/history",
            timeout=30
        )
        
        print(f"响应状态码: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            print("✅ SMS历史记录获取成功！")
            print(f"历史记录数量: {result.get('total', 0)}")
            print(f"响应数据: {json.dumps(result, indent=2, ensure_ascii=False)}")
        else:
            print(f"❌ SMS历史记录获取失败，状态码: {response.status_code}")
            print(f"错误信息: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print("❌ 连接失败！请确保Spring Boot应用正在运行在端口8080")
    except Exception as e:
        print(f"❌ 测试失败: {str(e)}")

def test_sms_statistics():
    """测试SMS统计信息功能"""
    print("=" * 50)
    print("📊 测试SMS统计信息功能")
    print("=" * 50)
    
    try:
        print(f"发送GET请求到: {SMS_API_URL}/statistics")
        
        response = requests.get(
            f"{SMS_API_URL}/statistics",
            timeout=30
        )
        
        print(f"响应状态码: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            print("✅ SMS统计信息获取成功！")
            print(f"响应数据: {json.dumps(result, indent=2, ensure_ascii=False)}")
        else:
            print(f"❌ SMS统计信息获取失败，状态码: {response.status_code}")
            print(f"错误信息: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print("❌ 连接失败！请确保Spring Boot应用正在运行在端口8080")
    except Exception as e:
        print(f"❌ 测试失败: {str(e)}")

def test_invalid_phone_number():
    """测试无效手机号码验证"""
    print("=" * 50)
    print("❌ 测试无效手机号码验证")
    print("=" * 50)
    
    # 测试无效手机号码
    invalid_numbers = [
        "123456",  # 太短
        "abcdefg",  # 包含字母
        "",  # 空字符串
        "1234567890123456789012345",  # 太长
    ]
    
    for phone_number in invalid_numbers:
        test_data = {
            "phoneNumber": phone_number,
            "message": "测试消息",
            "messageType": "TEST"
        }
        
        try:
            print(f"测试无效号码: '{phone_number}'")
            
            response = requests.post(
                f"{SMS_API_URL}/send",
                json=test_data,
                headers={"Content-Type": "application/json"},
                timeout=30
            )
            
            if response.status_code == 400:
                result = response.json()
                print(f"✅ 正确拒绝无效号码: {result.get('message', '未知错误')}")
            else:
                print(f"⚠️  意外响应状态码: {response.status_code}")
                
        except Exception as e:
            print(f"❌ 测试失败: {str(e)}")
        
        print("-" * 30)

def main():
    """主测试函数"""
    print("🚀 开始SMS功能全面测试")
    print("时间:", time.strftime("%Y-%m-%d %H:%M:%S"))
    print()
    
    # 执行所有测试
    test_send_sms()
    time.sleep(2)
    
    test_health_alert_sms()
    time.sleep(2)
    
    test_emergency_sms()
    time.sleep(2)
    
    test_sms_history()
    time.sleep(2)
    
    test_sms_statistics()
    time.sleep(2)
    
    test_invalid_phone_number()
    
    print("=" * 50)
    print("🎉 SMS功能测试完成！")
    print("=" * 50)

if __name__ == "__main__":
    main()
