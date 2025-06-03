package com.chikere.bp.bptracker.controller;

import com.chikere.bp.bptracker.service.AIChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling AI chat interactions from the sidebar.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class AIChatController {

    private final AIChatService aiChatService;

    /**
     * Endpoint for processing user questions and returning AI responses.
     *
     * @param request Map containing the user's question
     * @return ResponseEntity with the AI's response
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> processQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        log.info("Received question: {}", question);
        
        if (question == null || question.trim().isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Question cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        String response = aiChatService.processQuestion(question);
        
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("response", response);
        
        return ResponseEntity.ok(responseMap);
    }
}