package com.rice.trade.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rice.trade.config.ApiCryptoProperties;
import com.rice.trade.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

public class ApiCryptoFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiCryptoFilter.class);
    public static final String DECRYPTED_BODY_ATTRIBUTE = ApiCryptoFilter.class.getName() + ".DECRYPTED_BODY";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ObjectMapper objectMapper;
    private final ApiCryptoProperties properties;
    private final SecretKeySpec secretKey;

    public ApiCryptoFilter(ObjectMapper objectMapper, ApiCryptoProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.secretKey = createSecretKey(properties);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!isApiPath(request) || isPreflight(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpServletRequest requestToUse = request;
        if (properties.isEnabled()) {
            try {
                requestToUse = decryptRequestIfNeeded(request);
            } catch (GeneralSecurityException | IllegalArgumentException | JsonProcessingException ex) {
                reject(response, "请求报文解密失败");
                return;
            }
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(requestToUse);
        if (!properties.isEnabled()) {
            try {
                filterChain.doFilter(wrappedRequest, response);
            } finally {
                writeRequestLog(wrappedRequest);
            }
            return;
        }

        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            writeRequestLog(wrappedRequest);
        }
        encryptResponseIfNeeded(request, response, wrappedResponse);
    }

    private HttpServletRequest decryptRequestIfNeeded(HttpServletRequest request)
            throws IOException, GeneralSecurityException {
        if (!hasJsonBody(request)) {
            return request;
        }

        boolean encrypted = isEncrypted(request, properties.getRequestHeader());
        if (!encrypted) {
            if (properties.isRequireEncryptedRequests()) {
                throw new GeneralSecurityException("Encrypted request required");
            }
            return request;
        }

        byte[] encryptedBody = request.getInputStream().readAllBytes();
        if (encryptedBody.length == 0) {
            return request;
        }

        EncryptedEnvelope envelope = objectMapper.readValue(encryptedBody, EncryptedEnvelope.class);
        byte[] plainBody = decrypt(envelope);
        request.setAttribute(DECRYPTED_BODY_ATTRIBUTE, plainBody);
        return new BodyRequestWrapper(request, plainBody);
    }

    private void encryptResponseIfNeeded(
            HttpServletRequest request,
            HttpServletResponse response,
            ContentCachingResponseWrapper wrappedResponse
    ) throws IOException {
        byte[] body = wrappedResponse.getContentAsByteArray();
        if (!shouldEncryptResponse(request, wrappedResponse, body)) {
            wrappedResponse.copyBodyToResponse();
            return;
        }

        try {
            EncryptedEnvelope envelope = encrypt(body);
            byte[] envelopeBody = objectMapper.writeValueAsBytes(envelope);
            response.resetBuffer();
            response.setStatus(wrappedResponse.getStatus());
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader(properties.getResponseHeader(), "1");
            response.setContentLength(envelopeBody.length);
            response.getOutputStream().write(envelopeBody);
        } catch (GeneralSecurityException ex) {
            response.resetBuffer();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), ApiResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "响应报文加密失败",
                    null
            ));
        }
    }

    private boolean shouldEncryptResponse(
            HttpServletRequest request,
            ContentCachingResponseWrapper response,
            byte[] body
    ) {
        return isApiPath(request)
                && body.length > 0
                && response.getStatus() != HttpStatus.NO_CONTENT.value()
                && isJsonContent(response.getContentType())
                && !isEncrypted(response, properties.getResponseHeader());
    }

    private EncryptedEnvelope encrypt(byte[] plainBody) throws GeneralSecurityException {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        byte[] encrypted = cipher.doFinal(plainBody);
        return new EncryptedEnvelope(
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(encrypted)
        );
    }

    private byte[] decrypt(EncryptedEnvelope envelope) throws GeneralSecurityException {
        if (envelope == null || envelope.getIv() == null || envelope.getData() == null) {
            throw new GeneralSecurityException("Invalid encrypted envelope");
        }
        byte[] iv = Base64.getDecoder().decode(envelope.getIv());
        byte[] encrypted = Base64.getDecoder().decode(envelope.getData());
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        return cipher.doFinal(encrypted);
    }

    private SecretKeySpec createSecretKey(ApiCryptoProperties properties) {
        if (!properties.isEnabled()) {
            return null;
        }
        if (properties.getSharedKey() == null || properties.getSharedKey().isBlank()) {
            throw new IllegalStateException("app.crypto.shared-key must be set when app.crypto.enabled=true");
        }
        try {
            byte[] key = MessageDigest.getInstance("SHA-256")
                    .digest(properties.getSharedKey().getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.failure(
                HttpStatus.BAD_REQUEST.value(),
                message,
                null
        ));
    }

    private void writeRequestLog(ContentCachingRequestWrapper request) {
        try {
            log.info(
                    "API request params method={} path={} query={} body={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    toJson(requestParameters(request)),
                    toJson(requestBody(request))
            );
        } catch (RuntimeException ex) {
            log.warn("Failed to write API request params log", ex);
        }
    }

    private Map<String, Object> requestParameters(HttpServletRequest request) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values == null || values.length == 0) {
                parameters.put(key, "");
            } else if (values.length == 1) {
                parameters.put(key, values[0]);
            } else {
                parameters.put(key, Arrays.asList(values));
            }
        });
        return parameters;
    }

    private Object requestBody(ContentCachingRequestWrapper request) {
        byte[] content = decryptedBody(request);
        if (content == null) {
            content = request.getContentAsByteArray();
        }
        if (content.length == 0) {
            return Map.of();
        }

        String body = new String(content, requestCharset(request));
        if (body.isBlank()) {
            return Map.of();
        }

        if (isJsonContent(request.getContentType())) {
            try {
                return objectMapper.readTree(body);
            } catch (JsonProcessingException ignored) {
                return body;
            }
        }

        return body;
    }

    private byte[] decryptedBody(HttpServletRequest request) {
        Object value = request.getAttribute(DECRYPTED_BODY_ATTRIBUTE);
        if (value instanceof byte[] body) {
            return body;
        }
        return null;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return String.valueOf(value);
        }
    }

    private boolean hasJsonBody(HttpServletRequest request) {
        return ("POST".equalsIgnoreCase(request.getMethod())
                || "PUT".equalsIgnoreCase(request.getMethod())
                || "PATCH".equalsIgnoreCase(request.getMethod()))
                && isJsonContent(request.getContentType());
    }

    private boolean isApiPath(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

    private boolean isPreflight(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private boolean isJsonContent(String contentType) {
        return contentType != null && contentType.toLowerCase(Locale.ROOT).contains("json");
    }

    private Charset requestCharset(HttpServletRequest request) {
        String encoding = request.getCharacterEncoding();
        if (encoding == null || encoding.isBlank()) {
            return StandardCharsets.UTF_8;
        }

        try {
            return Charset.forName(encoding);
        } catch (RuntimeException ex) {
            return StandardCharsets.UTF_8;
        }
    }

    private boolean isEncrypted(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        return value != null && ("1".equals(value) || "true".equalsIgnoreCase(value));
    }

    private boolean isEncrypted(HttpServletResponse response, String headerName) {
        String value = response.getHeader(headerName);
        return value != null && ("1".equals(value) || "true".equalsIgnoreCase(value));
    }

    public static class EncryptedEnvelope {
        private String iv;
        private String data;

        public EncryptedEnvelope() {
        }

        public EncryptedEnvelope(String iv, String data) {
            this.iv = iv;
            this.data = data;
        }

        public String getIv() {
            return iv;
        }

        public void setIv(String iv) {
            this.iv = iv;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    private static class BodyRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] body;

        BodyRequestWrapper(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return inputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // Synchronous request processing is used by Spring MVC here.
                }

                @Override
                public int read() {
                    return inputStream.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), requestCharset()));
        }

        @Override
        public int getContentLength() {
            return body.length;
        }

        @Override
        public long getContentLengthLong() {
            return body.length;
        }

        @Override
        public String getContentType() {
            return MediaType.APPLICATION_JSON_VALUE;
        }

        private Charset requestCharset() {
            String encoding = getCharacterEncoding();
            if (encoding == null || encoding.isBlank()) {
                return StandardCharsets.UTF_8;
            }
            try {
                return Charset.forName(encoding);
            } catch (RuntimeException ex) {
                return StandardCharsets.UTF_8;
            }
        }
    }
}
