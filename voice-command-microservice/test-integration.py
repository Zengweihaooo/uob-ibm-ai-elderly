#!/usr/bin/env python3
"""
集成测试脚本 - 模拟完整的双路径处理流程
"""

import json
import time
from typing import Dict, Any

class MockMainProjectAIClient:
    """模拟主项目AI客户端"""
    
    def __init__(self):
        self.base_url = "http://localhost:8080"
    
    def chat_with_gemini(self, request: Dict[str, Any]) -> Dict[str, Any]:
        """模拟调用主项目AI对话服务"""
        message = request.get("message", "")
        
        # 模拟AI回复
        responses = {
            "你好": "你好！我是你的AI助手，有什么可以帮助你的吗？",
            "今天天气怎么样": "抱歉，我无法获取实时天气信息，建议您查看天气预报应用。",
            "讲个笑话": "为什么程序员总是分不清万圣节和圣诞节？因为 Oct 31 == Dec 25！",
            "谢谢": "不客气！很高兴能帮助到你。",
            "hello": "Hello! I'm your AI assistant. How can I help you today?",
            "how are you": "I'm doing well, thank you for asking! How are you?",
        }
        
        # 返回模拟响应
        response = responses.get(message, f"我理解您说的是：{message}。请告诉我更多信息。")
        
        return {
            "response": response,
            "status": "success",
            "model": "gemini-pro"
        }
    
    def is_main_project_ai_available(self) -> bool:
        """检查主项目AI服务是否可用"""
        return True

class MockFunctionRouterService:
    """模拟功能路由服务"""
    
    def execute_function(self, intent_result: Dict[str, Any]) -> Dict[str, Any]:
        """模拟功能执行"""
        function_name = intent_result.get("functionName", "")
        parameters = intent_result.get("parameters", {})
        
        # 模拟功能执行结果
        if function_name == "send_email":
            to_email = parameters.get("toEmail", "")
            subject = parameters.get("subject", "")
            return {
                "success": True,
                "message": f"邮件已成功发送给 {to_email}，主题：{subject}",
                "functionName": "send_email"
            }
        elif function_name == "view_schedule":
            return {
                "success": True,
                "message": "已为您显示日程安排",
                "functionName": "view_schedule"
            }
        elif function_name == "health_check":
            return {
                "success": True,
                "message": "健康检查完成，一切正常",
                "functionName": "health_check"
            }
        else:
            return {
                "success": False,
                "message": f"未知功能：{function_name}",
                "functionName": function_name
            }

def is_function_call_intent(user_text: str) -> bool:
    """意图检测逻辑"""
    lower_text = user_text.lower()
    
    function_keywords = [
        "发送邮件", "发邮件", "send email", "邮件", "email",
        "查看日程", "添加日程", "schedule", "日程", "calendar",
        "健康检查", "health check", "健康", "health",
        "联系人", "contact", "查找", "find",
        "重要日期", "important date", "生日", "birthday",
        "提醒", "reminder", "设置", "set"
    ]
    
    return any(keyword in lower_text for keyword in function_keywords)

def process_as_function_call(user_text: str, function_router: MockFunctionRouterService) -> Dict[str, Any]:
    """模拟功能调用路径"""
    print(f"  🔧 执行功能调用路径...")
    
    # 模拟AI意图分析
    intent_result = {
        "functionName": "send_email" if "邮件" in user_text or "email" in user_text.lower() else "unknown",
        "confidence": 0.9,
        "parameters": {
            "toEmail": "test@example.com",
            "subject": "测试邮件",
            "content": "这是一封测试邮件"
        },
        "reasoning": "基于关键词识别为邮件发送功能"
    }
    
    # 执行功能
    execution_result = function_router.execute_function(intent_result)
    
    return {
        "success": execution_result["success"],
        "feedbackText": execution_result["message"],
        "path": "功能调用路径",
        "functionName": intent_result["functionName"]
    }

def process_as_normal_chat(user_text: str, ai_client: MockMainProjectAIClient) -> Dict[str, Any]:
    """模拟普通对话路径"""
    print(f"  💬 执行普通对话路径...")
    
    # 调用主项目AI服务
    request = {"message": user_text}
    ai_response = ai_client.chat_with_gemini(request)
    
    return {
        "success": True,
        "feedbackText": ai_response["response"],
        "path": "普通对话路径",
        "aiModel": ai_response["model"]
    }

def process_with_intent_prejudgment(user_text: str) -> Dict[str, Any]:
    """模拟双路径处理逻辑"""
    print(f"📝 处理用户输入: '{user_text}'")
    
    # 初始化模拟服务
    ai_client = MockMainProjectAIClient()
    function_router = MockFunctionRouterService()
    
    # 意图预判
    if is_function_call_intent(user_text):
        print(f"  🎯 检测到功能调用意图")
        return process_as_function_call(user_text, function_router)
    else:
        print(f"  🎯 检测到普通对话意图")
        return process_as_normal_chat(user_text, ai_client)

def run_integration_test():
    """运行集成测试"""
    print("🚀 集成测试 - 双路径处理机制")
    print("=" * 60)
    
    test_cases = [
        # 功能调用测试
        "发送邮件给张三，主题是会议提醒",
        "查看明天的日程安排",
        "健康检查一下",
        "添加联系人李四",
        "设置生日提醒",
        
        # 普通对话测试
        "你好，最近怎么样？",
        "今天天气怎么样？",
        "讲个笑话给我听",
        "现在几点了？",
        "谢谢你的帮助",
        "hello, how are you?",
    ]
    
    results = []
    
    for i, test_input in enumerate(test_cases, 1):
        print(f"\n🔍 测试用例 {i}:")
        start_time = time.time()
        
        result = process_with_intent_prejudgment(test_input)
        processing_time = (time.time() - start_time) * 1000
        
        result["processingTime"] = f"{processing_time:.2f}ms"
        result["input"] = test_input
        results.append(result)
        
        print(f"  ✅ 结果: {result['feedbackText']}")
        print(f"  ⏱️  处理时间: {result['processingTime']}")
        print(f"  🛤️  处理路径: {result['path']}")
    
    # 统计结果
    print("\n" + "=" * 60)
    print("📊 测试统计")
    print("=" * 60)
    
    function_calls = sum(1 for r in results if r["path"] == "功能调用路径")
    normal_chats = sum(1 for r in results if r["path"] == "普通对话路径")
    success_count = sum(1 for r in results if r["success"])
    
    print(f"📈 总测试用例: {len(test_cases)}")
    print(f"🔧 功能调用路径: {function_calls}")
    print(f"💬 普通对话路径: {normal_chats}")
    print(f"✅ 成功处理: {success_count}")
    print(f"❌ 失败处理: {len(test_cases) - success_count}")
    
    avg_time = sum(float(r["processingTime"].replace("ms", "")) for r in results) / len(results)
    print(f"⏱️  平均处理时间: {avg_time:.2f}ms")
    
    print("\n🎉 集成测试完成！")

if __name__ == "__main__":
    run_integration_test()
