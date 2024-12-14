package org.alpermelkeli.firebase;

import org.alpermelkeli.util.ConfigUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * The FirebaseAuthService class provides methods to interact with Firebase Authentication.
 * It allows user authentication and account creation using Firebase Authentication API.
 */
@Service
public class FirebaseAuthService {

    public Map<String, Object> authenticateUser(String email, String password) {

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);
        requestBody.put("returnSecureToken", true);

        String url = ConfigUtil.getProperty("FIREBASE_SIGN_IN_WITH_EMAIL") + "?key=" + ConfigUtil.getProperty("FIREBASE_API_KEY");

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, new HttpHeaders()),
                    Map.class
            );
            Map<String, Object> responseBody = response.getBody();
            responseBody.put("success", true);
            return responseBody;

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    public String createUser(String email, String password){
        return "";
    }
}

