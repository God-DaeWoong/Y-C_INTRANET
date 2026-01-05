package com.ync.intranet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 네이버웍스 OAuth 서비스
 */
@Service
public class NaverWorksOAuthService {

    @Value("${naverworks.oauth.client-id}")
    private String clientId;

    @Value("${naverworks.oauth.client-secret}")
    private String clientSecret;

    @Value("${naverworks.oauth.redirect-uri}")
    private String redirectUri;

    @Value("${naverworks.oauth.authorization-uri}")
    private String authorizationUri;

    @Value("${naverworks.oauth.token-uri}")
    private String tokenUri;

    @Value("${naverworks.oauth.user-info-uri}")
    private String userInfoUri;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public NaverWorksOAuthService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 네이버웍스 로그인 URL 생성
     */
    public String getAuthorizationUrl(String state) {
        return authorizationUri +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=user" +
                "&response_type=code" +
                "&state=" + state;
    }

    /**
     * Authorization Code로 Access Token 발급
     */
    public String getAccessToken(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("grant_type", "authorization_code");
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    tokenUri,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();

        } catch (Exception e) {
            throw new RuntimeException("Access Token 발급 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Access Token으로 사용자 정보 조회
     */
    public Map<String, Object> getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    userInfoUri,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("email", jsonNode.has("email") ? jsonNode.get("email").asText() : null);
            userInfo.put("name", jsonNode.has("name") ? jsonNode.get("name").asText() : null);
            userInfo.put("userId", jsonNode.has("userId") ? jsonNode.get("userId").asText() : null);
            userInfo.put("telephoneNumber", jsonNode.has("telephoneNumber") ? jsonNode.get("telephoneNumber").asText() : null);
            userInfo.put("mobilePhone", jsonNode.has("mobilePhone") ? jsonNode.get("mobilePhone").asText() : null);
            userInfo.put("department", jsonNode.has("department") ? jsonNode.get("department").asText() : null);
            userInfo.put("position", jsonNode.has("position") ? jsonNode.get("position").asText() : null);
            userInfo.put("employeeNumber", jsonNode.has("employeeNumber") ? jsonNode.get("employeeNumber").asText() : null);

            return userInfo;

        } catch (Exception e) {
            throw new RuntimeException("사용자 정보 조회 실패: " + e.getMessage(), e);
        }
    }
}
