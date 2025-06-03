package com.chikere.bp.bptracker.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Service for handling AI chat interactions in the sidebar.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIChatService {

    private final ChatClient chatClient;
    private final MeterRegistry meterRegistry;

    // Create counters for monitoring
    private Counter aiChatSuccessCounter;
    private Counter aiChatFailureCounter;

    /**
     * Initialize counters after constructor
     */
    @PostConstruct
    public void initCounters() {
        this.aiChatSuccessCounter = meterRegistry.counter("ai.chat.success");
        this.aiChatFailureCounter = meterRegistry.counter("ai.chat.failure");
    }

    /**
     * Process a user question and get a response from the AI.
     *
     * @param question The user's question
     * @return The AI's response
     */
    public String processQuestion(String question) {
        log.info("Processing user question: {}", question);

        try {
            // Send question to LLM via Spring AI
            String response = chatClient.prompt(question).call().content();

            // Increment success counter
            aiChatSuccessCounter.increment();

            log.debug("AI response: {}", response);
            return response;
        } catch (Exception e) {
            // Increment failure counter
            aiChatFailureCounter.increment();

            log.error("Error calling AI service: {}", e.getMessage(), e);
            return "I'm sorry, I couldn't process your question at the moment. Please try again later.";
        }
    }
}
