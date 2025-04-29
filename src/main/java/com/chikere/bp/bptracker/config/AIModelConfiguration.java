package com.chikere.bp.bptracker.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIModelConfiguration {
    /**
     * Creates and configures a ChatClient bean with medical expertise capabilities.
     * <p>
     * The configured ChatClient has the following features:
     * <ul>
     *   <li>A default system prompt that positions the AI as an expert medical professional</li>
     *   <li>Memory capabilities to maintain conversation context</li>
     *   <li>Logging functionality for monitoring interactions</li>
     * </ul>
     * </p>
     *
     * @param chatClientBuilder The builder instance for creating a ChatClient
     * @return A fully configured ChatClient instance
     */


    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder
                .defaultSystem("You are an expert medical professional known for providing clear, accurate, and well-reasoned answers. Always think step by step before arriving at a conclusion. Use sound clinical judgment and explain your reasoning in a way that is both informative and easy to understand.")
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()), new SimpleLoggerAdvisor())
                .build();
    }

}