package com.example.demo.service;

import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.User;
import com.example.demo.pojo.EmailVerificationResult;
import com.example.demo.pojo.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 邮箱验证服务 - 专门处理用户注册时的邮箱验证
 * 
 * @author AI Assistant
 * @version 1.0
 */
@Service
public class EmailVerificationService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * 邮箱验证服务 - 专门处理用户注册时的邮箱验证
     */
    public EmailVerificationResult verifyEmailForRegistration(String email) {
        EmailVerificationResult result = new EmailVerificationResult();
        
        try {
            // 1. 基础格式验证
            ValidationResult formatResult = validateEmailFormat(email);
            if (!formatResult.isValid()) {
                logValidationFailure(email, formatResult.getErrorCode(), formatResult.getMessage());
                result.setSuccess(false);
                result.setMessage(formatResult.getMessage());
                result.setErrorCode(formatResult.getErrorCode());
                return result;
            }
            
            // 2. 域名验证
            ValidationResult domainResult = validateEmailDomain(email);
            if (!domainResult.isValid()) {
                logValidationFailure(email, domainResult.getErrorCode(), domainResult.getMessage());
                result.setSuccess(false);
                result.setMessage(domainResult.getMessage());
                result.setErrorCode(domainResult.getErrorCode());
                return result;
            }
            
            // 3. 重复检查
            ValidationResult duplicateResult = checkEmailDuplicate(email);
            if (!duplicateResult.isValid()) {
                logValidationFailure(email, duplicateResult.getErrorCode(), duplicateResult.getMessage());
                result.setSuccess(false);
                result.setMessage(duplicateResult.getMessage());
                result.setErrorCode(duplicateResult.getErrorCode());
                return result;
            }
            
            // 4. 安全检查
            ValidationResult securityResult = performSecurityChecks(email);
            if (!securityResult.isValid()) {
                logValidationFailure(email, securityResult.getErrorCode(), securityResult.getMessage());
                result.setSuccess(false);
                result.setMessage(securityResult.getMessage());
                result.setErrorCode(securityResult.getErrorCode());
                return result;
            }
            
            // 5. 验证通过
            logValidationSuccess(email);
            result.setSuccess(true);
            result.setMessage("邮箱验证通过，可以注册");
            result.setEmail(email);
            
            return result;
            
        } catch (Exception e) {
            logValidationFailure(email, "VALIDATION_ERROR", e.getMessage());
            result.setSuccess(false);
            result.setMessage("邮箱验证过程中发生错误: " + e.getMessage());
            result.setErrorCode("VALIDATION_ERROR");
            return result;
        }
    }
    
    /**
     * 发送验证邮件
     */
    public EmailVerificationResult sendVerificationEmail(String email) {
        EmailVerificationResult result = new EmailVerificationResult();
        
        try {
            // 再次验证邮箱
            EmailVerificationResult validationResult = verifyEmailForRegistration(email);
            if (!validationResult.isSuccess()) {
                return validationResult;
            }
            
            // 生成验证码
            String verificationCode = generateVerificationCode();
            
            // 发送验证邮件
            emailService.sendVerificationEmail(email, verificationCode);
            
            result.setSuccess(true);
            result.setMessage("验证邮件发送成功");
            result.setEmail(email);
            result.setVerificationCode(verificationCode);
            
            return result;
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("发送验证邮件失败: " + e.getMessage());
            result.setErrorCode("EMAIL_SEND_ERROR");
            return result;
        }
    }
    
    /**
     * 验证邮箱格式
     */
    private ValidationResult validateEmailFormat(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ValidationResult(false, "邮箱地址不能为空", "EMAIL_EMPTY");
        }
        
        // 更严格的邮箱格式验证
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!email.matches(emailRegex)) {
            return new ValidationResult(false, "邮箱格式不正确，请检查邮箱地址", "EMAIL_FORMAT_INVALID");
        }
        
        // 检查邮箱长度
        if (email.length() > 100) {
            return new ValidationResult(false, "邮箱地址过长，请使用较短的邮箱地址", "EMAIL_TOO_LONG");
        }
        
        // 检查特殊字符
        if (email.contains("..") || email.contains("__") || email.contains("--")) {
            return new ValidationResult(false, "邮箱地址包含无效字符", "EMAIL_INVALID_CHARS");
        }
        
        return new ValidationResult(true, "邮箱格式正确", "FORMAT_VALID");
    }
    
    /**
     * 验证邮箱域名
     */
    private ValidationResult validateEmailDomain(String email) {
        String domain = email.substring(email.indexOf("@") + 1);
        
        // 检查常见无效域名
        List<String> invalidDomains = Arrays.asList(
            "example.com", "test.com", "invalid.com", "localhost", 
            "temp.com", "demo.com", "fake.com", "spam.com"
        );
        
        if (invalidDomains.contains(domain.toLowerCase())) {
            return new ValidationResult(false, "请使用有效的邮箱域名，不能使用测试域名", "INVALID_DOMAIN");
        }
        
        // 检查域名长度
        if (domain.length() < 3 || domain.length() > 50) {
            return new ValidationResult(false, "邮箱域名长度不正确", "DOMAIN_LENGTH_INVALID");
        }
        
        // 检查域名格式
        if (!domain.matches("^[a-zA-Z0-9.-]+$")) {
            return new ValidationResult(false, "邮箱域名包含无效字符", "DOMAIN_INVALID_CHARS");
        }
        
        return new ValidationResult(true, "邮箱域名有效", "DOMAIN_VALID");
    }
    
    /**
     * 检查邮箱重复
     */
    private ValidationResult checkEmailDuplicate(String email) {
        try {
            User existingUser = userMapper.findByEmail(email.trim().toLowerCase());
            if (existingUser != null) {
                String message = "该邮箱已被注册";
                if (existingUser.getStatus() == User.UserStatus.PENDING) {
                    message += "，但尚未验证。您可以重新发送验证码";
                } else if (existingUser.getStatus() == User.UserStatus.VERIFIED) {
                    message += "。如果您忘记密码，请联系管理员";
                }
                return new ValidationResult(false, message, "EMAIL_ALREADY_EXISTS");
            }
            return new ValidationResult(true, "邮箱可用", "EMAIL_AVAILABLE");
        } catch (Exception e) {
            return new ValidationResult(false, "检查邮箱时发生错误", "DATABASE_ERROR");
        }
    }
    
    /**
     * 安全检查
     */
    private ValidationResult performSecurityChecks(String email) {
        // 检查是否包含可疑内容
        String lowerEmail = email.toLowerCase();
        List<String> suspiciousPatterns = Arrays.asList(
            "admin", "root", "test", "temp", "spam", "fake"
        );
        
        for (String pattern : suspiciousPatterns) {
            if (lowerEmail.contains(pattern)) {
                return new ValidationResult(false, "邮箱地址包含可疑内容", "SUSPICIOUS_EMAIL");
            }
        }
        
        return new ValidationResult(true, "安全检查通过", "SECURITY_VALID");
    }
    
    /**
     * 生成验证码
     */
    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }
    
    /**
     * 记录验证成功
     */
    private void logValidationSuccess(String email) {
        System.out.println("Email validation successful for: " + email);
    }
    
    /**
     * 记录验证失败
     */
    private void logValidationFailure(String email, String errorCode, String message) {
        System.err.println("Email validation failed for: " + email + " - " + errorCode + ": " + message);
    }
}