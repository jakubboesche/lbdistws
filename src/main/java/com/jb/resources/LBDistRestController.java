package com.jb.resources;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LBDistRestController {

    @RequestMapping("/")
    public String index() {
        return "Greetings from LBDistWS!";
    }
}
