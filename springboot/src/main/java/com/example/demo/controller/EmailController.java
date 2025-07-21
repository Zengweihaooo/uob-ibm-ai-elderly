package com.example.demo.controller;

import com.example.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling email-related operations
 * 
 * This controller manages email subscription and sending functionality
 * for the IBM AI Elderly Project.
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Controller
public class EmailController {

    @Autowired
    private EmailService emailService;

    /**
     * Display the email management page
     * 
     * @param model Spring MVC model to add attributes
     * @return The email form template name
     */
    @GetMapping("/emails")
    public String showPage(Model model) {
        model.addAttribute("emailList", emailService.getAllEmails());
        return "emailForm";
    }

    /**
     * Add a new email to the subscription list
     * 
     * @param email The email address to add
     * @param model Spring MVC model to add attributes
     * @return The email form template name with success message
     */
    @PostMapping("/add-email")
    public String addEmail(@RequestParam("email") String email, Model model) {
        emailService.addEmail(email);
        model.addAttribute("emailList", emailService.getAllEmails());
        model.addAttribute("message", "Successfully added: " + email);
        return "emailForm";
    }

    /**
     * Send test emails to all subscribed users
     * 
     * @param model Spring MVC model to add attributes
     * @return The email form template name with success message
     */
    @PostMapping("/send-all")
    public String sendAllEmails(Model model) {
        emailService.sendEmailsToAll();
        model.addAttribute("emailList", emailService.getAllEmails());
        model.addAttribute("message", "Test emails have been sent!");
        return "emailForm";
    }
}