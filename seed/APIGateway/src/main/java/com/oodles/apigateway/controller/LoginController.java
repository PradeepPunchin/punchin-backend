package com.oodles.apigateway.controller;


import com.oodles.apigateway.domain.RedisUserRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @PostMapping("/api/v1/user/login")
    ResponseEntity<Object> login(){
       // RedisUserRole userRole = RedisUserRole.of(user);
        Map<String, Object> data = new HashMap<>();
        data.put("token", "ABCD");
        return  new ResponseEntity<>(
                data,
                HttpStatus.OK);
    }
    @GetMapping("/api/v1/user/details")
    public ResponseEntity<Object> getUser(){
        //   RedisUserRole user = AppUtils.getLoggedInUser();
        // UserDto userDto = userFeignService.getUserDetails(user.getUserId());
        return new ResponseEntity<>(
                "User get Details",
                HttpStatus.OK);
    }
}
