package org.alpermelkeli.Rest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

public interface UserRestApiInterface {

    @PostMapping("/loginUser")
    Map<String, Object> loginUser(@RequestBody Map<String, String> credentials);

}
