package com.punchin.controllers;

import com.punchin.dto.PageDTO;
import com.punchin.service.HomeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
@Slf4j
public class HomeController {

    @Autowired
    HomeService homeService;

    @RequestMapping(method = RequestMethod.GET, value = "/api/hello")
    public String sayHello() {
        return "Swagger Hello World";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/claimData/upload")
    public ResponseEntity<Object> uploadExcelData(@RequestParam("file") MultipartFile file) {
        try {
            Object uploadResponse = homeService.saveUploadExcelData(file);
            if (uploadResponse != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(uploadResponse);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid format");

        } catch (Exception e) {
            log.error("Exception upload claim data file", e);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload excel file ");
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getAllClaimData")
    public ResponseEntity<Object> getAllClaimData(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "5") Integer limit) {
        try {
            log.info("Get Claim Data pagination list request received :: page: {}, limit: {}", page, limit);
            PageDTO allUploadedData = homeService.getAllProducts(page, limit);
            if (!Objects.isNull(allUploadedData)) {
                return ResponseEntity.status(HttpStatus.OK).body(allUploadedData);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Data Not Found ");
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Data Not Found ");
    }


}
