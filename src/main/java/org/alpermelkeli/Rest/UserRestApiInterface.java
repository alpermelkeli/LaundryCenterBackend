package org.alpermelkeli.Rest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

public interface UserRestApiInterface {

    @PostMapping("/loginUser")
    Map<String, Object> loginUser(@RequestBody Map<String, String> credentials);
    @GetMapping("/getUserBalance")
    Map<String, Double> getUserBalance(@RequestParam String userId);
}
