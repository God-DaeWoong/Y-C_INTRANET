package com.ync.intranet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 네이버웍스 OAuth 서비스
 * - 타임아웃: 10초
 * - 민감 정보 로깅 마스킹 처리
 * - 상세한 에러 처리
 */
@Service
public class NaverWorksOAuthService {

    private static final Logger log = LoggerFactory.getLogger(NaverWorksOAuthService.class);

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

    public NaverWorksOAuthService(RestTemplateBuilder restTemplateBuilder) {
        // 타임아웃 설정: 연결 10초, 읽기 10초
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 네이버웍스 로그인 URL 생성
     */
    public String getAuthorizationUrl(String state) {
        return authorizationUri +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=user.profile.read" +
                "&response_type=code" +
                "&state=" + state;
    }

    /**
     * Authorization Code로 Access Token 발급
     */
    public String getAccessToken(String code) {
        try {
            log.info("네이버웍스 Access Token 발급 시작");

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
            String accessToken = jsonNode.get("access_token").asText();

            // 토큰 마스킹 처리 (보안)
            String maskedToken = accessToken.length() > 10 ?
                accessToken.substring(0, 10) + "..." : "***";
            log.info("Access Token 발급 완료 (마스킹): {}", maskedToken);

            return accessToken;

        } catch (HttpClientErrorException e) {
            // 401, 403, 404 등 클라이언트 에러
            log.error("네이버웍스 토큰 발급 실패 (클라이언트 에러) - Status: {}, Body: {}",
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("네이버웍스 인증에 실패했습니다. 관리자에게 문의하세요.", e);
        } catch (HttpServerErrorException e) {
            // 5xx 서버 에러
            log.error("네이버웍스 서버 오류 - Status: {}", e.getStatusCode());
            throw new RuntimeException("네이버웍스 서버에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", e);
        } catch (Exception e) {
            log.error("Access Token 발급 중 예외 발생", e);
            throw new RuntimeException("네이버웍스 로그인 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * Access Token으로 사용자 정보 조회
     */
    public Map<String, Object> getUserInfo(String accessToken) {
        try {
            log.info("네이버웍스 사용자 정보 조회 시작");

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

            // 디버깅: 전체 응답 로깅 (JSON 포맷팅)
            log.info("=== 네이버웍스 API 전체 응답 ===");
            log.info("{}", jsonNode.toPrettyString());
            log.info("=== 응답 종료 ===");

            // 모든 필드 체크 및 로깅
            log.info("📋 받은 필드 목록:");
            jsonNode.fieldNames().forEachRemaining(fieldName -> {
                JsonNode fieldValue = jsonNode.get(fieldName);
                log.info("  - {}: {} (타입: {})",
                    fieldName,
                    fieldValue.isNull() ? "null" : fieldValue.toString(),
                    fieldValue.getNodeType());
            });

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("email", jsonNode.has("email") ? jsonNode.get("email").asText() : null);
            userInfo.put("userId", jsonNode.has("userId") ? jsonNode.get("userId").asText() : null);

            // 전화번호: telephone 또는 cellPhone
            String phone = null;
            if (jsonNode.has("cellPhone") && !jsonNode.get("cellPhone").isNull()) {
                phone = jsonNode.get("cellPhone").asText();
            } else if (jsonNode.has("telephone") && !jsonNode.get("telephone").isNull()) {
                phone = jsonNode.get("telephone").asText();
            }
            userInfo.put("telephoneNumber", phone);
            userInfo.put("mobilePhone", phone);
            userInfo.put("employeeNumber", jsonNode.has("employeeNumber") ? jsonNode.get("employeeNumber").asText() : null);

            // 이름: userName.lastName + userName.firstName
            String name = null;
            if (jsonNode.has("userName") && !jsonNode.get("userName").isNull()) {
                JsonNode userNameNode = jsonNode.get("userName");
                String lastName = userNameNode.has("lastName") ? userNameNode.get("lastName").asText() : "";
                String firstName = userNameNode.has("firstName") ? userNameNode.get("firstName").asText() : "";
                name = (lastName + firstName).trim();
                if (name.isEmpty()) {
                    name = null;
                }
            }
            userInfo.put("name", name);

            // 부서, 직급: organizations[0].orgUnits[0]에서 추출
            String department = null;
            String position = null;
            if (jsonNode.has("organizations") && jsonNode.get("organizations").isArray() &&
                jsonNode.get("organizations").size() > 0) {
                JsonNode orgNode = jsonNode.get("organizations").get(0);
                if (orgNode.has("orgUnits") && orgNode.get("orgUnits").isArray() &&
                    orgNode.get("orgUnits").size() > 0) {
                    JsonNode orgUnitNode = orgNode.get("orgUnits").get(0);
                    // 부서명: orgUnitName
                    if (orgUnitNode.has("orgUnitName") && !orgUnitNode.get("orgUnitName").isNull()) {
                        department = orgUnitNode.get("orgUnitName").asText();
                    }
                    // 직급: positionName
                    if (orgUnitNode.has("positionName") && !orgUnitNode.get("positionName").isNull()) {
                        position = orgUnitNode.get("positionName").asText();
                    }
                }
            }
            userInfo.put("department", department);
            userInfo.put("position", position);

            // 이메일 마스킹 처리 (보안)
            String email = (String) userInfo.get("email");
            String maskedEmail = email != null && email.contains("@") ?
                email.substring(0, 3) + "***@" + email.split("@")[1] : "***";
            log.info("사용자 정보 조회 완료 - 이메일(마스킹): {}, 이름: {}",
                maskedEmail, userInfo.get("name"));

            // Null 필드 감지 및 경고
            if (userInfo.get("name") == null) {
                log.warn("⚠️ 네이버웍스 API에서 'name' 필드가 null입니다.");
            }
            if (userInfo.get("userId") == null) {
                log.warn("⚠️ 네이버웍스 API에서 'userId' 필드가 null입니다.");
            }
            if (userInfo.get("email") == null) {
                log.error("❌ 네이버웍스 API에서 'email' 필드가 null입니다! (필수값)");
            }

            return userInfo;

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                log.error("네이버웍스 사용자 정보 조회 권한 부족 - Status: {}", e.getStatusCode());
                throw new RuntimeException("네이버웍스 정보 조회 권한이 부족합니다. 관리자에게 권한 요청하세요.", e);
            } else if (e.getStatusCode().value() == 404) {
                log.error("네이버웍스 사용자 정보를 찾을 수 없음");
                throw new RuntimeException("네이버웍스 사용자 정보를 찾을 수 없습니다.", e);
            } else {
                log.error("네이버웍스 사용자 정보 조회 실패 - Status: {}", e.getStatusCode());
                throw new RuntimeException("사용자 정보 조회에 실패했습니다.", e);
            }
        } catch (HttpServerErrorException e) {
            log.error("네이버웍스 서버 오류 - Status: {}", e.getStatusCode());
            throw new RuntimeException("네이버웍스 서버에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", e);
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 예외 발생", e);
            throw new RuntimeException("사용자 정보 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 민감 정보 마스킹 유틸리티 메서드
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 3) {
            return "*".repeat(localPart.length()) + "@" + domain;
        }
        return localPart.substring(0, 3) + "***@" + domain;
    }
}
