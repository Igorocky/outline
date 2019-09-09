package org.igye.outline2.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.igye.outline2.OutlineUtils.readStringFromClasspath;

@Controller
@RequestMapping("/fe")
@ResponseBody
public class FeController {
    @Value("${app.version}")
    private String appVersion;
    @Value("${config.name}")
    private String configName;

    @GetMapping("/**")
    public ResponseEntity<byte[]> index() throws IOException {
        return new ResponseEntity<>(
                readStringFromClasspath("/web/index.html")
                        .replaceAll("@app\\.version@", appVersion)
                        .replaceAll("@config\\.name@", configName)
                        .getBytes(StandardCharsets.UTF_8),
                HttpStatus.OK
        );
    }
}
