package com.punchin.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Service
@Transactional
public class AmazonS3FileManagers {

    public static final String AWS_END_POINT = "s3.ap-south-1.amazonaws.com";
    public static final String S_3_SIGNER_TYPE = "AWSS3V4SignerType";

    @Value("${amazon.client.bucket.name}")
    private String bucketName;

    @Value("${amazon.client.access.key}")
    private String awsAccessKey;

    @Value("${amazon.client.secret.key}")
    private String awsSecretKey;
    /**
     * Get Access Credentials from Environment variable.
     */
    AWSCredentials creds = new AWSCredentials() {

        @Override
        public String getAWSAccessKeyId() {
            return awsAccessKey;
        }

        @Override
        public String getAWSSecretKey() {
            return awsSecretKey;
        }
    };

    /**
     * To Create Connection Form AmazonClient.
     *
     * @return AmazonS3 Connection.
     */


    public String uploadFileToAmazonS3(String key, File uncompressedFile, String name) throws IOException {
//        String path = System.getProperty("user.dir") + "/";
//        File compressedFile = new File(path + name);
//        getCompressedFile(uncompressedFile, compressedFile);
        AmazonS3 client = getAmazonConnection();
        PutObjectRequest por = new PutObjectRequest(bucketName, key + name, uncompressedFile);
        por.setCannedAcl(CannedAccessControlList.Private);
        client.putObject(por).getVersionId();
        return client.getUrl(bucketName, key + name).toString();
    }

    public String uploadFile(String claimId, MultipartFile multipartFile, String folderName) {
        try {
            String versionId = null;
            File file = convertMultiPartToFile(multipartFile);
            String extension = "." + FilenameUtils.getExtension(multipartFile.getOriginalFilename());
            String fileName = claimId + "-" + System.currentTimeMillis() + extension;
            versionId = uploadFileToAmazonS3(folderName, file, fileName);
            cleanUp(file);
            return versionId;
        } catch (IOException e) {
            return null;
        }
    }

    public void cleanUp(File file) {
        try {
            Files.delete(file.toPath());
        } catch (Exception e) {

        }
    }

    public File convertMultiPartToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename());
        try {
            multipartFile.transferTo(file);
        } catch (Exception ex) {

        }
        return file;
    }

    public void getCompressedFile(File source_filepath, File destinaton_zip_filepath) {
        byte[] buffer = new byte[1024];
        try {
            FileOutputStream fileOutputStream =new FileOutputStream(destinaton_zip_filepath);
            GZIPOutputStream gzipOuputStream = new GZIPOutputStream(fileOutputStream);
            FileInputStream fileInput = new FileInputStream(source_filepath);
            int bytes_read;
            while ((bytes_read = fileInput.read(buffer)) > 0) {
                gzipOuputStream.write(buffer, 0, bytes_read);
            }
            fileInput.close();
            gzipOuputStream.finish();
            gzipOuputStream.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public AmazonS3 getAmazonConnection() {
        AmazonS3 s3 = new AmazonS3Client(creds, new ClientConfiguration().withSignerOverride(S_3_SIGNER_TYPE));
        s3.setEndpoint(AWS_END_POINT);
        return s3;
    }


    public ByteArrayOutputStream downloadFile(String keyName) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            AmazonS3 client = getAmazonConnection();
            S3Object s3object = client.getObject(new GetObjectRequest(bucketName, keyName));

            InputStream is = s3object.getObjectContent();

            int len;
            byte[] buffer = new byte[4096];
            while ((len = is.read(buffer, 0, buffer.length)) != -1) {
                outputStream.write(buffer, 0, len);
            }

            return outputStream;
        } catch (IOException ioException) {
            log.error("IOException: " + ioException.getMessage());
        } catch (AmazonServiceException serviceException) {
            log.info("AmazonServiceException Message:    " + serviceException.getMessage());
            throw serviceException;
        } catch (AmazonClientException clientException) {
            log.info("AmazonClientException Message: " + clientException.getMessage());
            throw clientException;
        }

        return null;
    }

    public InputStream getStreamFromS3(String docUrl) {
        try {
            AmazonS3 client = getAmazonConnection();
            S3Object s3object = client.getObject(new GetObjectRequest(bucketName,
                    "agent/" + FilenameUtils.getName(docUrl)));
            return s3object.getObjectContent();
        }  catch (Exception e) {
            return null;
        }
    }
}
