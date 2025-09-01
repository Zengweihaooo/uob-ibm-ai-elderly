#!/usr/bin/env python3
# Python script to test intent detection logic


def is_function_call(text: str) -> bool:
    """Simulate intent detection logic in Java"""
    lower = text.lower()
    # Function call keywords (English only)
    keywords = [
        "send email", "email",
        "schedule", "calendar",
        "health check", "health",
        "contact", "find",
        "important date", "birthday",
        "reminder", "set"
    ]
    # If contains any function keywords, consider as function call
    return any(k in lower for k in keywords)


def test_intent_detection():
    """Test intent detection"""
    print("🧪 Testing intent detection logic")

    # Test cases
    cases = [
        # Function calls
        ("send email to Zhang San", True),
        ("view schedule", True),
        ("add contact", True),
        ("set reminder", True),
        ("important date", True),
        # Normal chats
        ("hello", False),
        ("what's the weather today", False),
        ("tell me a joke", False),
        ("what time is it", False),
        ("thanks", False),
    ]

    passed = 0
    total = len(cases)
    for text, expected in cases:
        result = is_function_call(text)
        status = "✅ PASS" if result == expected else "❌ FAIL"
        if result == expected:
            passed += 1
        print(f"{status} input: '{text}' -> expected: {expected}, actual: {result}")

    print(f"Results: {passed}/{total} passed")
    if passed == total:
        print("🎉 All tests passed! Intent detection logic is correct.")
    else:
        print("⚠️  Some tests failed, please check the logic.")


def test_dual_path():
    """Test dual-path processing logic"""
    print("\n🔄 Testing dual-path processing logic")
    texts = [
        "send email to Zhang San, subject is meeting reminder",
        "what's the weather today?",
        "view schedule for tomorrow",
        "hello, how are you?",
        "health check please",
        "tell me a joke",
    ]
    for text in texts:
        is_function = is_function_call(text)
        path = "Function path" if is_function else "Normal chat path"
        print(f"📝 input: '{text}'")
        print(f"   → path: {path}")


if __name__ == "__main__":
    test_intent_detection()
    test_dual_path()
