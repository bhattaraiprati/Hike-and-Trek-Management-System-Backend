package com.example.treksathi.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrganizerController {

    @GetMapping("/api/greet")
    public String greet(HttpServletRequest request){
        return "Hello from trek sathi";
    }

}
