package org.igye.outline;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ControllerT {

    @GetMapping("hello")
    public String hello() {
        return "HELLO!!!";
    }
}
