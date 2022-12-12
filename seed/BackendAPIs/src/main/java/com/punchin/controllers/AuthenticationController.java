package com.punchin.controllers;

import com.punchin.service.AuthenticationService;
import com.punchin.dto.LoginRequestDTO;
import com.punchin.utility.ResponseHandler;
import com.punchin.utility.constant.MessageCode;
import com.punchin.utility.constant.UrlMapping;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;


@CrossOrigin
@Slf4j
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MessageSource messageSource;

    @ApiOperation(value = "User Login", notes = "This can be used to generate API token")
    @PostMapping(value = UrlMapping.LOGIN)
    public ResponseEntity<Object> userAuthentication(@ApiParam(name = "Credential", value = "The Login request Object for user login", required = true) @Valid @RequestBody LoginRequestDTO credentials){
        try{
            log.info("AuthenticationController :: userAuthentication LoginRequestDTO{}", credentials);
                log.info("LOGIN BY CREDENTIALS");
                Map<String, Object> resultMap = authenticationService.authenticateUserAccount(credentials);
                if(resultMap.get("message").equals(MessageCode.success)){
                    return ResponseHandler.response(resultMap.get("session"), MessageCode.success, true, HttpStatus.OK);
                    //return ResponseHandler.response(resultMap.get("session"), messageSource.getMessage(MessageCode.success, null, LocaleContextHolder.getLocale()), true, HttpStatus.OK);
                }
            return ResponseHandler.response(null, messageSource.getMessage(MessageCode.invalidCredentials, null, LocaleContextHolder.getLocale()), true, HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            log.error("EXCEPTION WHILE AuthenticationController :: userAuthentication e{}", e);
            return ResponseHandler.response(null, messageSource.getMessage(MessageCode.backText, null, LocaleContextHolder.getLocale()), false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "User Logout", notes = "This can be used to logout the system")
    @GetMapping(value = UrlMapping.LOGOUT)
    public ResponseEntity<Object> logout(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        authenticationService.logout(request, result);
        return ResponseHandler.response(null, messageSource.getMessage(MessageCode.logout, null, LocaleContextHolder.getLocale()), true, HttpStatus.OK);
    }
}
