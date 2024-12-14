package org.alpermelkeli.Rest;


import org.springframework.web.bind.annotation.GetMapping;

public interface ServerRestApiInterface {
    @GetMapping("/getTime")
    String getTime();
}
