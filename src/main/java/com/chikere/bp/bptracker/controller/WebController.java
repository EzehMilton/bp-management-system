package com.chikere.bp.bptracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Legacy controller that redirects to the home page.
 * All functionality has been moved to specialized controllers:
 * - PatientController
 * - ReadingController
 * - RiskAssessmentController
 * - ApiController
 */
@Controller
public class WebController {

    /**
     * Redirect to home page
     */
    @GetMapping("/web")
    public String redirectToHome() {
        return "redirect:/";
    }
}
