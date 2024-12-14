package org.alpermelkeli.Rest;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/server")
@Service
public class ServerRestController implements ServerRestApiInterface{
    @Override
    public String getTime() {
        return System.currentTimeMillis() + "";
    }
}
