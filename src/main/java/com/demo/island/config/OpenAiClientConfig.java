package com.demo.island.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.ApiKey;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Minimal OpenAI ChatClient wiring for the PlayerAgent when running in lab mode.
 */
@Configuration
public class OpenAiClientConfig {

    @Bean
    public ChatClient chatClient(
            @Value("${spring.ai.openai.api-key:}") String apiKeyProp,
            @Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}") String model) {
        // Prefer explicit property, then env vars; never log the key.
        String apiKey = firstNonBlank(apiKeyProp,
                System.getenv("SPRING_AI_OPENAI_API_KEY"),
                System.getenv("OPENAI_API_KEY"));
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key is required when spring.ai.openai.enabled=true");
        }

        OpenAiApi openAiApi = new OpenAiApi(
                "https://api.openai.com",
                () -> apiKey,
                new LinkedMultiValueMap<>(),
                "/v1/chat/completions",
                "/v1/embeddings",
                RestClient.builder(),
                WebClient.builder(),
                new DefaultResponseErrorHandler()
        );

        OpenAiChatOptions options = new OpenAiChatOptions();
        options.setModel(model);

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .observationRegistry(ObservationRegistry.NOOP)
                .build();

        return ChatClient.builder(chatModel).build();
    }

    private static String firstNonBlank(String... candidates) {
        if (candidates == null) return null;
        for (String c : candidates) {
            if (c != null && !c.isBlank()) {
                return c;
            }
        }
        return null;
    }
}
