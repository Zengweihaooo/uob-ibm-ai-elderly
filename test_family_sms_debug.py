#!/usr/bin/env python3
"""
家庭联系人短信发送调试脚本
用于诊断 /api/family/contacts/{id}/message 接口的问题

使用方法：
python test_family_sms_debug.py

作者：AI Assistant
版本：1.0
"""

import requests
import json
import time

# 配置
BASE_URL = "http://localhost:8080"
FAMILY_API_URL = f"{BASE_URL}/api/family"
AUTH_API_URL = f"{BASE_URL}/user"

def test_user_login():
    """测试用户登录获取token"""
    print("=" * 50)
    print("🔐 测试用户登录")
    print("=" * 50)
    
    # 测试登录数据 - 使用你系统中已存在的用户
    login_data = {
        "email": "test@example.com",
        "password": "password123"
    }
    
    try:
        print(f"发送POST请求到: {AUTH_API_URL}/login")
        print(f"登录数据: {json.dumps(login_data, indent=2)}")
        
        response = requests.post(
            f"{AUTH_API_URL}/login",
            data=login_data,  # 使用 form data 而不是 JSON
            timeout=30
        )
        
        print(f"登录响应状态码: {response.status_code}")
        print(f"登录响应内容: {response.text}")
        
        if response.status_code == 200:
            result = response.json()
            if result.get("success") and result.get("token"):
                print("✅ 用户登录成功！")
                return result["token"]
            else:
                print("❌ 登录失败，未获得token")
                return None
        else:
            print(f"❌ 登录失败，状态码: {response.status_code}")
            return None
            
    except Exception as e:
        print(f"❌ 登录异常: {str(e)}")
        return None

def test_get_family_contacts(token):
    """测试获取家庭联系人列表"""
    print("=" * 50)
    print("👨‍👩‍👧‍👦 测试获取家庭联系人")
    print("=" * 50)
    
    if not token:
        print("❌ 没有有效的token，跳过测试")
        return None
    
    try:
        headers = {
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json"
        }
        
        print(f"发送GET请求到: {FAMILY_API_URL}/contacts")
        print(f"请求头: {json.dumps(headers, indent=2)}")
        
        response = requests.get(
            f"{FAMILY_API_URL}/contacts",
            headers=headers,
            timeout=30
        )
        
        print(f"响应状态码: {response.status_code}")
        print(f"响应内容: {response.text}")
        
        if response.status_code == 200:
            result = response.json()
            if result.get("success") and result.get("contacts"):
                contacts = result["contacts"]
                print(f"✅ 获取到 {len(contacts)} 个联系人")
                if contacts:
                    contact_id = contacts[0].get("id")
                    print(f"第一个联系人ID: {contact_id}")
                    return contact_id
                else:
                    print("⚠️  联系人列表为空")
                    return None
            else:
                print("❌ 获取联系人失败")
                return None
        else:
            print(f"❌ 获取联系人失败，状态码: {response.status_code}")
            return None
            
    except Exception as e:
        print(f"❌ 获取联系人异常: {str(e)}")
        return None

def test_send_sms_to_contact(token, contact_id):
    """测试发送短信给联系人"""
    print("=" * 50)
    print("📱 测试发送短信给联系人")
    print("=" * 50)
    
    if not token or not contact_id:
        print("❌ 缺少token或联系人ID，跳过测试")
        return False
    
    # 测试数据
    message_data = {
        "message": "这是一条测试短信，来自家庭联系人系统调试！",
        "type": "sms"
    }
    
    try:
        headers = {
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json"
        }
        
        url = f"{FAMILY_API_URL}/contacts/{contact_id}/message"
        print(f"发送POST请求到: {url}")
        print(f"请求头: {json.dumps(headers, indent=2)}")
        print(f"请求数据: {json.dumps(message_data, indent=2, ensure_ascii=False)}")
        
        response = requests.post(
            url,
            headers=headers,
            json=message_data,
            timeout=30
        )
        
        print(f"响应状态码: {response.status_code}")
        print(f"响应内容: {response.text}")
        
        if response.status_code == 200:
            result = response.json()
            if result.get("success"):
                print("✅ 短信发送成功！")
                return True
            else:
                print(f"❌ 短信发送失败: {result.get('message', '未知错误')}")
                return False
        elif response.status_code == 400:
            print("❌ 400 Bad Request - 请求格式或数据有问题")
            try:
                error_detail = response.json()
                print(f"错误详情: {json.dumps(error_detail, indent=2, ensure_ascii=False)}")
            except:
                print(f"原始错误响应: {response.text}")
            return False
        elif response.status_code == 401:
            print("❌ 401 Unauthorized - 认证失败")
            return False
        elif response.status_code == 404:
            print("❌ 404 Not Found - 联系人不存在")
            return False
        else:
            print(f"❌ 意外的状态码: {response.status_code}")
            return False
            
    except Exception as e:
        print(f"❌ 发送短信异常: {str(e)}")
        return False

def test_direct_sms_api(token):
    """测试直接SMS API"""
    print("=" * 50)
    print("📲 测试直接SMS API")
    print("=" * 50)
    
    if not token:
        print("❌ 没有有效的token，跳过测试")
        return False
    
    sms_data = {
        "phoneNumber": "+8613800138000",
        "message": "直接SMS API测试消息",
        "messageType": "TEST"
    }
    
    try:
        headers = {
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json"
        }
        
        url = f"{BASE_URL}/api/sms/send"
        print(f"发送POST请求到: {url}")
        print(f"请求数据: {json.dumps(sms_data, indent=2, ensure_ascii=False)}")
        
        response = requests.post(
            url,
            headers=headers,
            json=sms_data,
            timeout=30
        )
        
        print(f"响应状态码: {response.status_code}")
        print(f"响应内容: {response.text}")
        
        if response.status_code == 200:
            print("✅ 直接SMS API工作正常")
            return True
        else:
            print("❌ 直接SMS API失败")
            return False
            
    except Exception as e:
        print(f"❌ 直接SMS API异常: {str(e)}")
        return False

def main():
    """主测试函数"""
    print("🚀 开始家庭联系人短信发送调试")
    print("时间:", time.strftime("%Y-%m-%d %H:%M:%S"))
    print()
    
    # 步骤1：登录获取token
    token = test_user_login()
    time.sleep(1)
    
    # 步骤2：获取联系人列表
    contact_id = test_get_family_contacts(token)
    time.sleep(1)
    
    # 步骤3：测试发送短信给联系人
    if contact_id:
        sms_success = test_send_sms_to_contact(token, contact_id)
        time.sleep(1)
    else:
        print("⚠️  跳过家庭联系人短信测试（没有联系人）")
        sms_success = False
    
    # 步骤4：测试直接SMS API
    direct_sms_success = test_direct_sms_api(token)
    
    print()
    print("=" * 50)
    print("📊 调试结果汇总")
    print("=" * 50)
    print(f"登录状态: {'✅ 成功' if token else '❌ 失败'}")
    print(f"联系人获取: {'✅ 成功' if contact_id else '❌ 失败'}")
    print(f"家庭联系人短信: {'✅ 成功' if sms_success else '❌ 失败'}")
    print(f"直接SMS API: {'✅ 成功' if direct_sms_success else '❌ 失败'}")
    
    if not sms_success:
        print()
        print("🔧 排查建议:")
        if not token:
            print("1. 检查用户登录功能")
            print("2. 确认用户数据库中有测试用户")
        elif not contact_id:
            print("1. 检查家庭联系人数据库表")
            print("2. 确认有联系人数据")
        else:
            print("1. 检查SMS服务配置")
            print("2. 查看服务器日志")
            print("3. 验证联系人电话号码格式")
    
    print("=" * 50)

if __name__ == "__main__":
    main()
