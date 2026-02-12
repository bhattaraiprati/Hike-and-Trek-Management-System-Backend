package com.example.treksathi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SendGridEmailSendServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private SendGridEmailSendService sendGridEmailSendService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sendGridEmailSendService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(sendGridEmailSendService, "fromEmail", "test@example.com");

        when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    void testSendSimpleEmailAsync_Success() {
        // Mocking WebClient chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity())
                .thenReturn(Mono.just(org.springframework.http.ResponseEntity.ok().build()));

        CompletableFuture<Boolean> result = sendGridEmailSendService.sendSimpleEmailAsync("recipient@example.com",
                "Subject", "Text");

        assertTrue(result.join());
    }
}
