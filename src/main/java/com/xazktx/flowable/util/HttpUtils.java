package com.xazktx.flowable.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class HttpUtils {

    private static RestTemplate restTemplate = new RestTemplate();

    private HttpUtils() {
    }

    public static ResponseEntity<Object> getWithAuth(String url, String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, request, Object.class);
    }

    public static ResponseEntity<Object> postWithAuth(String url, String username, String password, Object req) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        MediaType type = MediaType.parseMediaType("application/json;charset=UTF-8");
        headers.setContentType(type);
        HttpEntity<Object> request = new HttpEntity<>(req, headers);
        return restTemplate.exchange(url, HttpMethod.POST, request, Object.class);
    }

}
