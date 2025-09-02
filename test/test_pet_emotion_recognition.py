#!/usr/bin/env python3
"""
Pet Emotion Recognition Test
测试宠物AI的情感识别能力
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

def test_pet_emotion_recognition():
    """Test pet emotion recognition with various messages"""
    
    # Generate fresh JWT token
    token = generate_jwt_token()
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    
    print("=== 宠物情感识别测试 ===")
    print(f"测试时间: {time.strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"JWT Token: {token[:50]}...")
    print()
    
    # 测试用例
    test_cases = [
        {
            "message": "I'm not happy today",
            "expected": "negative",
            "description": "否定情感 - 应该识别为负面情绪"
        },
        {
            "message": "I'm happy today!",
            "expected": "positive", 
            "description": "正面情感 - 应该识别为正面情绪"
        },
        {
            "message": "I'm feeling sad",
            "expected": "negative",
            "description": "直接负面情感"
        },
        {
            "message": "I don't feel good",
            "expected": "negative",
            "description": "否定词 + 正面词"
        },
        {
            "message": "I'm not feeling great",
            "expected": "negative",
            "description": "否定词 + 正面词"
        },
        {
            "message": "I'm feeling wonderful!",
            "expected": "positive",
            "description": "强烈正面情感"
        },
        {
            "message": "I'm tired and lonely",
            "expected": "negative",
            "description": "多个负面词汇"
        },
        {
            "message": "Hello, how are you?",
            "expected": "greeting",
            "description": "问候语"
        }
    ]
    
    results = []
    
    for i, test_case in enumerate(test_cases, 1):
        print(f"测试 {i}: {test_case['description']}")
        print(f"输入: \"{test_case['message']}\"")
        
        try:
            response = requests.post(
                "http://localhost:8080/api/pet/message",
                headers=headers,
                json={"message": test_case['message']},
                timeout=10
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get("success"):
                    pet_response = data.get("petResponse", {})
                    content = pet_response.get("content", "")
                    
                    print(f"宠物回复: \"{content}\"")
                    
                    # 分析回复类型
                    if "sorry" in content.lower() or "feeling this way" in content.lower():
                        actual_type = "negative"
                    elif "happy to hear" in content.lower() or "positive energy" in content.lower():
                        actual_type = "positive"
                    elif "hello" in content.lower() or "excited to chat" in content.lower():
                        actual_type = "greeting"
                    else:
                        actual_type = "neutral"
                    
                    # 判断测试结果
                    if actual_type == test_case['expected']:
                        print(f"✅ 测试通过! 期望: {test_case['expected']}, 实际: {actual_type}")
                        results.append(True)
                    else:
                        print(f"❌ 测试失败! 期望: {test_case['expected']}, 实际: {actual_type}")
                        results.append(False)
                    
                else:
                    print(f"❌ API错误: {data.get('message')}")
                    results.append(False)
            else:
                print(f"❌ HTTP错误: {response.status_code}")
                print(f"Response: {response.text}")
                results.append(False)
                
        except Exception as e:
            print(f"❌ 请求失败: {e}")
            results.append(False)
        
        print("-" * 60)
    
    # 总结测试结果
    print("\n" + "=" * 60)
    print("测试结果总结")
    print("=" * 60)
    
    passed = sum(results)
    total = len(results)
    success_rate = (passed / total) * 100 if total > 0 else 0
    
    print(f"总测试数: {total}")
    print(f"通过测试: {passed}")
    print(f"失败测试: {total - passed}")
    print(f"成功率: {success_rate:.1f}%")
    
    if success_rate >= 80:
        print("🎉 宠物情感识别优化成功!")
        print("✅ 能够正确识别否定词和负面情感")
        print("✅ 能够正确识别正面情感")
        print("✅ 情感识别逻辑得到改善")
    else:
        print("❌ 宠物情感识别还需要进一步优化")
        print("❌ 建议检查情感识别逻辑")
    
    print("=" * 60)

def main():
    """Run the pet emotion recognition test"""
    print("=" * 60)
    print("宠物情感识别优化测试")
    print("=" * 60)
    print()
    
    test_pet_emotion_recognition()

if __name__ == "__main__":
    main()
