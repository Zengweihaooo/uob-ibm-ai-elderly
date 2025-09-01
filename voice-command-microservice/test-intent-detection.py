#!/usr/bin/env python3
"""
测试意图检测逻辑的Python脚本
"""

def is_function_call_intent(user_text):
    """模拟Java中的意图检测逻辑"""
    lower_text = user_text.lower()
    
    # 功能调用关键词
    function_keywords = [
        "发送邮件", "发邮件", "send email", "邮件", "email",
        "查看日程", "添加日程", "schedule", "日程", "calendar",
        "健康检查", "health check", "健康", "health",
        "联系人", "contact", "查找", "find",
        "重要日期", "important date", "生日", "birthday",
        "提醒", "reminder", "设置", "set"
    ]
    
    # 如果包含功能关键词，认为是功能调用
    is_function_call = any(keyword in lower_text for keyword in function_keywords)
    
    return is_function_call

def test_intent_detection():
    """测试意图检测功能"""
    print("🧪 测试意图检测逻辑")
    print("=" * 50)
    
    # 测试用例
    test_cases = [
        # 功能调用测试
        ("发送邮件给张三", True),
        ("send email to john", True),
        ("查看日程", True),
        ("health check", True),
        ("添加联系人", True),
        ("设置提醒", True),
        ("重要日期", True),
        
        # 普通对话测试
        ("你好", False),
        ("今天天气怎么样", False),
        ("hello", False),
        ("how are you", False),
        ("讲个笑话", False),
        ("现在几点了", False),
        ("谢谢", False),
    ]
    
    passed = 0
    total = len(test_cases)
    
    for text, expected in test_cases:
        result = is_function_call_intent(text)
        status = "✅" if result == expected else "❌"
        print(f"{status} 输入: '{text}' -> 预期: {expected}, 实际: {result}")
        
        if result == expected:
            passed += 1
    
    print("=" * 50)
    print(f"测试结果: {passed}/{total} 通过")
    
    if passed == total:
        print("🎉 所有测试通过！意图检测逻辑正确。")
    else:
        print("⚠️  部分测试失败，需要检查逻辑。")

def test_dual_path_logic():
    """测试双路径处理逻辑"""
    print("\n🔄 测试双路径处理逻辑")
    print("=" * 50)
    
    test_inputs = [
        "发送邮件给张三，主题是会议提醒",
        "今天天气怎么样？",
        "查看明天的日程安排",
        "你好，最近怎么样？",
        "健康检查一下",
        "讲个笑话给我听",
    ]
    
    for text in test_inputs:
        is_function = is_function_call_intent(text)
        path = "功能调用路径" if is_function else "普通对话路径"
        print(f"📝 输入: '{text}'")
        print(f"   → 路径: {path}")
        print()

if __name__ == "__main__":
    test_intent_detection()
    test_dual_path_logic()
