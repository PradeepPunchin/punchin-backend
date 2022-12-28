package com.punchin.service;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.GZIPOutputStream;

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
}
