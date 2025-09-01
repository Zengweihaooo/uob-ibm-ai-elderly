#!/usr/bin/env python3
# Integration test script - simulate full dual-path processing flow

import time

class MainProjectAIClientMock:
    """Mock main project AI client"""
    def chat_with_gemini(self, message: str):
        """Mock calling main project's AI chat service"""
        # Mock AI responses
        responses = {
            "hello": "Hello! I'm your AI assistant. How can I help you?",
            "how's the weather today": "Sorry, I can't fetch real-time weather. Please check a weather app.",
            "tell me a joke": "Why do programmers confuse Halloween with Christmas? Because Oct 31 == Dec 25!",
            "thanks": "You're welcome! Happy to help.",
        }
        # Return mock response
        response = responses.get(message, f"I understand you said: {message}. Please tell me more.")
        return {"response": response}

    def health(self):
        """Check if the main project AI service is available"""
        return {"status": "ok"}

class FunctionRouterServiceMock:
    """Mock function router service"""
    def execute(self, function_name: str, params: dict):
        """Mock function execution"""
        start = time.time()
        # Mock execution result
        if function_name == "send_email":
            to_email = params.get("toEmail", "unknown@example.com")
            subject = params.get("subject", "Test")
            result = {
                "success": True,
                "message": f"Email sent to {to_email}, subject: {subject}",
            }
        elif function_name == "view_schedule":
            result = {"success": True, "message": "Schedule displayed for you"}
        elif function_name == "health_check":
            result = {"success": True, "message": "Health check done, all good"}
        else:
            result = {"success": False, "message": f"Unknown function: {function_name}"}
        end = time.time()
        result["processingTime"] = int((end - start) * 1000)
        return result

# Intent detection logic

def is_function_call(text: str) -> bool:
    keywords = [
        "send email", "email",
        "view schedule", "add schedule", "schedule", "calendar",
        "health check", "health",
        "contact", "find",
        "important date", "birthday",
        "reminder", "set"
    ]
    lower = text.lower()
    return any(k in lower for k in keywords)

# Mock function path

def process_as_function_call(user_text: str):
    print(f"  🔧 Execute function-call path...")
    # Mock AI intent analysis
    intent = {
        "functionName": "send_email" if ("email" in user_text.lower()) else "unknown",
        "parameters": {
            "toEmail": "john@example.com",
            "subject": "Test Email",
            "content": "This is a test email"
        },
        "reasoning": "Identified as email sending based on keywords"
    }
    # Execute function
    router = FunctionRouterServiceMock()
    exec_result = router.execute(intent["functionName"], intent["parameters"])
    return {
        "success": exec_result["success"],
        "feedbackText": exec_result["message"],
        "processingTime": exec_result["processingTime"],
        "path": "function-call path",
    }

# Mock normal chat path

def process_as_normal_chat(user_text: str):
    print(f"  💬 Execute normal chat path...")
    # Call main project AI service
    ai = MainProjectAIClientMock()
    resp = ai.chat_with_gemini(user_text)
    return {
        "success": True,
        "feedbackText": resp.get("response", ""),
        "processingTime": 10,
        "path": "normal chat path",
    }

# Dual-path processing logic

def process_user_input(user_text: str):
    print(f"📝 Processing user input: '{user_text}'")
    ai = MainProjectAIClientMock()
    router = FunctionRouterServiceMock()
    # Intent pre-judgment
    if is_function_call(user_text):
        print(f"  🎯 Detected function-call intent")
        return process_as_function_call(user_text)
    else:
        print(f"  🎯 Detected normal chat intent")
        return process_as_normal_chat(user_text)

# Run integration test

def run_tests():
    print("🚀 Integration Test - Dual-Path Processing")
    test_cases = [
        # Function-call tests
        "send email to John, subject is meeting reminder",
        "view tomorrow's schedule",
        "health check please",
        "add contact Jane",
        "set birthday reminder",
        # Normal chat tests
        "hello, how are you?",
        "how's the weather today?",
        "tell me a joke",
        "what time is it now?",
        "thanks for your help",
    ]
    results = []
    for i, case in enumerate(test_cases, 1):
        print(f"\n🔍 Test Case {i}:")
        result = process_user_input(case)
        results.append(result)
        print(f"  ✅ Result: {result['feedbackText']}")
        print(f"  ⏱️  Processing time: {result['processingTime']}")
        print(f"  🛤️  Path: {result['path']}")
    # Stats
    print("📊 Test Stats")
    function_calls = sum(1 for r in results if r["path"] == "function-call path")
    normal_chats = sum(1 for r in results if r["path"] == "normal chat path")
    success_count = sum(1 for r in results if r["success"])
    avg_time = sum(r["processingTime"] for r in results) / len(results)
    print(f"📈 Total cases: {len(test_cases)}")
    print(f"🔧 Function-call path: {function_calls}")
    print(f"💬 Normal chat path: {normal_chats}")
    print(f"✅ Success: {success_count}")
    print(f"❌ Failed: {len(test_cases) - success_count}")
    print(f"⏱️  Average processing time: {avg_time:.2f}ms")
    print("\n🎉 Integration test finished!")

if __name__ == "__main__":
    run_tests()
