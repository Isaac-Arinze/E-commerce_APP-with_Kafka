package com.sky_ecommerce.checkout.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Paystack test integration:
 * - POST /api/payments/paystack/initialize
 * - GET  /api/payments/paystack/verify?reference=...
 * - POST /api/payments/paystack/webhook
 *
 * Notes:
 * - Amount to Paystack must be in kobo (NGN minor units). We multiply Naira by 100 on initialize.
 * - Callback URL is sent to Paystack initialize request (from env PAYSTACK_CALLBACK_URL).
 * - Webhook signature (x-paystack-signature) is validated using HMAC-SHA512 of the raw request body with PAYSTACK_SECRET_KEY.
 */
@RestController
@RequestMapping("/api/payments/paystack")
public class PaymentController {

    private final WebClient paystackClient;
    private final String callbackUrl;
    private final String secretKey;
    private final String publicKey;

    public PaymentController(
            @Value("${paystack.base-url:https://api.paystack.co}") String baseUrl,
            @Value("${paystack.secret-key:}") String secretKey,
            @Value("${paystack.public-key:}") String publicKey,
            @Value("${paystack.callback-url:https://example.com}") String callbackUrl
    ) {
        this.secretKey = secretKey;
        this.publicKey = publicKey;
        this.callbackUrl = callbackUrl;
        this.paystackClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> Mono.just(clientRequest));
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> Mono.just(clientResponse));
    }

    // DTOs
    public record InitializeRequest(
            @NotNull @Min(1) Long amount, // Naira
            @NotBlank @Email String email,
            String orderId
    ) {}

    public record InitializeResponse(
            boolean status,
            String message,
            Data data
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Data {
            @JsonProperty("authorization_url")
            public String authorizationUrl;
            @JsonProperty("access_code")
            public String accessCode;
            public String reference;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerifyResponse {
        public boolean status;
        public String message;
        public VerifyData data;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class VerifyData {
            public Long amount;
            public String currency;
            public String status; // "success", "failed", etc.
            public String reference;
            @JsonProperty("gateway_response")
            public String gatewayResponse;
            public String channel;
            public String ip_address;
        }
    }

    // Initialize payment
    @PostMapping("/initialize")
    public Mono<ResponseEntity<?>> initialize(@Valid @RequestBody InitializeRequest req) {
        // Convert naira to kobo
        long amountInKobo = req.amount * 100;

        Map<String, Object> payload = Map.of(
                "email", req.email,
                "amount", amountInKobo,
                "callback_url", callbackUrl,
                "metadata", Map.of(
                        "orderId", StringUtils.hasText(req.orderId) ? req.orderId : "",
                        "provider", "paystack"
                )
        );

        return paystackClient.post()
                .uri("/transaction/initialize")
                .body(BodyInserters.fromValue(payload))
                .exchangeToMono(resp -> handleResponse(resp, InitializeResponse.class))
                .map(body -> ResponseEntity.status(HttpStatus.OK).body(body));
    }

    // Verify payment
    @GetMapping("/verify")
    public Mono<ResponseEntity<?>> verify(@RequestParam("reference") String reference) {
        return paystackClient.get()
                .uri("/transaction/verify/{reference}", reference)
                .exchangeToMono(resp -> handleResponse(resp, VerifyResponse.class))
                .map(body -> ResponseEntity.ok(body));
    }

    private <T> Mono<T> handleResponse(ClientResponse resp, Class<T> type) {
        if (resp.statusCode().is2xxSuccessful()) {
            return resp.bodyToMono(type);
        }
        return resp.bodyToMono(String.class)
                .defaultIfEmpty("")
                .flatMap(body -> Mono.error(new RuntimeException("Paystack API error " + resp.statusCode() + ": " + body)));
    }

    // Webhook endpoint
    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(@RequestHeader(name = "x-paystack-signature", required = false) String signature,
                                     @RequestBody String rawBody) {
        if (!StringUtils.hasText(signature)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Missing signature"));
        }
        if (!isValidSignature(rawBody, signature, secretKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid signature"));
        }

        // Minimal handling: parse event and mark paid on charge.success
        // We keep it generic here, actual order update can be wired to OrderService if needed.
        Map<String, Object> result = Map.of("received", true);
        return ResponseEntity.ok(result);
    }

    private boolean isValidSignature(String payload, String signature, String secret) {
        try {
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512Hmac.init(keySpec);
            byte[] macData = sha512Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(macData.length * 2);
            for (byte b : macData) {
                sb.append(String.format("%02x", b));
            }
            String computed = sb.toString();
            return computed.equalsIgnoreCase(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
