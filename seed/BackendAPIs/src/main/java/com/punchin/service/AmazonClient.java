package com.punchin.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Date;

@Slf4j
@Service
public class AmazonClient {

    @Value("${amazon.client.bucket.name}")
    private String bucketName;

    @Value("${amazon.client.access.key}")
    private String accessKey;

    @Value("${amazon.client.secret.key}")
    private String secretKey;

    @Value("${amazon.client.bucket.endpoint.url}")
    private String endpointUrl;

    private AmazonS3 s3client;

    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.s3client = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }

    public File convertMultiPartToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename());
        try {
            multipartFile.transferTo(file);
            log.info("File created successfully");
        } catch (IOException e) {
            log.error("Exception in file convert service :: {}", e);
        } catch (Exception ex) {
            log.error("Exception in file convert service :: {}", ex);
        }
        return file;
    }

    void uploadFileTos3bucket(String fileName, File file) {
        try {
            this.s3client.putObject(new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AmazonClient :: uploadFileTos3bucket ", e);
        }
    }

    void deleteLocalFile(File file) {
        try {
            if (file != null) {
                Files.delete(file.toPath());
            }
        } catch (IOException e) {
            log.error("Exception while deleting file from local:: {}", e.getMessage());
        }
    }

    public String uploadFile(String claimId, MultipartFile multipartFile) {
        try {
            File file = convertMultiPartToFile(multipartFile);
            String extension = "." + FilenameUtils.getExtension(multipartFile.getOriginalFilename());
            String fileName = claimId + "-" + System.currentTimeMillis() + extension;
            uploadFileTos3bucket(fileName, file);
            deleteLocalFile(file);
            return endpointUrl + fileName;
        } catch (IOException e) {
            log.error("Exception while uploading file from local:: {}", e.getMessage());
            return null;
        }
    }

    public String uploadFile(File file) {
        try {
            //String extension = "." + FilenameUtils.getExtension(file.getName());
            String fileName = file.getName();
            uploadFileTos3bucket(fileName, file);
            deleteLocalFile(file);
            return endpointUrl + fileName;
        } catch (Exception e) {
            log.error("Exception while uploading file from local:: {}", e.getMessage());
            return null;
        }
    }

}
