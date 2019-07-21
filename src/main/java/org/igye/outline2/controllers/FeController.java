package org.igye.outline2.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/fe")
public class FeController {

    @GetMapping("/**")
    public String index() {
        return "index";
    }
}
