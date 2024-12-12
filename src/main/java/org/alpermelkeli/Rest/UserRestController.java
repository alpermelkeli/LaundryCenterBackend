package org.alpermelkeli.Rest;

import org.alpermelkeli.MQTT.MQTTControllerService;
import org.alpermelkeli.firebase.FirebaseAuthService;
import org.alpermelkeli.firebase.FirebaseFirestoreService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserRestController implements UserRestApiInterface{
    private final FirebaseAuthService firebaseAuthService;
    private final FirebaseFirestoreService firebaseFirestoreService;

    public UserRestController(FirebaseFirestoreService firebaseFirestoreService, FirebaseAuthService firebaseAuthService) {
        this.firebaseFirestoreService = firebaseFirestoreService;
        this.firebaseAuthService = firebaseAuthService;
    }

    @Override
    public Map<String, Object> loginUser(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        Map<String, Object> errorResponse = new HashMap<>();
        if (email == null || password == null) {


            return errorResponse;
        }
        return firebaseAuthService.authenticateUser(email, password);
    }

    @Override
    public Map<String, Double> getUserBalance(String userId) {
        return firebaseFirestoreService.getUserBalance(userId);
    }

}
