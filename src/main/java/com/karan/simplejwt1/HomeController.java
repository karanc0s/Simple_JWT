package com.karan.simplejwt1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
public class HomeController {

    @GetMapping("/secure")
    public ResponseEntity<String> secure(){
        return ResponseEntity.ok("You reached secure endpoint");
    }
}
