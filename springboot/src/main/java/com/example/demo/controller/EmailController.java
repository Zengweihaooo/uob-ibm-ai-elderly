package com.example.demo.controller;

import com.example.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class EmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/emails")
    public String showPage(Model model) {
        model.addAttribute("emailList", emailService.getAllEmails());
        return "emailForm";
    }

    @PostMapping("/add-email")
    public String addEmail(@RequestParam("email") String email, Model model) {
        emailService.addEmail(email);
        model.addAttribute("emailList", emailService.getAllEmails());
        model.addAttribute("message", "添加成功：" + email);
        return "emailForm";
    }

    @PostMapping("/send-all")
    public String sendAllEmails(Model model) {
        emailService.sendEmailsToAll();
        model.addAttribute("emailList", emailService.getAllEmails());
        model.addAttribute("message", "测试邮件已发送！");
        return "emailForm";
    }
}