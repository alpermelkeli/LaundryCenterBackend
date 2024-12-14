package org.alpermelkeli.Rest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * UserRestApiInterface defines a contract for user-related operations
 * through RESTful API endpoints. It provides methods to handle user
 * authentication (login) and to retrieve user-specific financial information
 * such as balance.
 */
public interface UserRestApiInterface {

    @PostMapping("/loginUser")
    Map<String, Object> loginUser(@RequestBody Map<String, String> credentials);
    @GetMapping("/getUserBalance")
    Map<String, Double> getUserBalance(@RequestParam String userId);
}
