package com.punchin.security;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
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
        this.s3client = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_SOUTHEAST_1).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }

    public File convertMultiPartToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename());
        try {
            multipartFile.transferTo(file);
            log.info("File created successfully");
        } catch (IOException e) {
            log.error("Exception in file convert service :: {}", e);
        } catch (Exception ex){
            log.error("Exception in file convert service :: {}", ex);
        }
        return file;
    }

    void uploadFileTos3bucket(String fileName, File file) {
        try {
            this.s3client.putObject(new PutObjectRequest(bucketName, fileName, file));//withCannedAcl(CannedAccessControlList.PublicRead));
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

    public String generateTempS3Url(String key) {
        try{
            // Set the presigned URL to expire after one hour.
            Date expiration = new Date();
            //expiration time set 5 minute
            int expTimeInMS = 100;
            long expTimeMillis = expiration.getTime() + expTimeInMS * 60 * 60;
            expiration.setTime(expTimeMillis);

            // Generate the presigned URL.
            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, key.replace("https://pando2.s3.ap-southeast-1.amazonaws.com/", "")).withMethod(HttpMethod.GET).withExpiration(expiration);

            URL url = this.s3client.generatePresignedUrl(generatePresignedUrlRequest);
            log.info("temperory URL is : url{} and expiration time is time{}", url, expiration);

            return url.toString();

        }catch (Exception e){
            log.error("EXCEPTION OCCURRED WHILE GENERATING S3 FILE TEMPORARY URL... e{}", e);
            return null;
        }
    }
}
