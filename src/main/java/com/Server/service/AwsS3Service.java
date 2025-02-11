package com.Server.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.Server.exception.OurException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

@Service
public class AwsS3Service {
    private final String bucketName = "royal-hotel-server";

    @Value("${aws.s3.access.key}")
    private String awsS3AccessKey;

    @Value("${aws.s3.secret.key}")
    private String awsS3SecreteKey;

    public String saveImageToS3(MultipartFile photo) {
        try {
            String s3FileName = photo.getOriginalFilename();

            // Khởi tạo credentials AWS
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsS3AccessKey, awsS3SecreteKey);

            // Tạo client kết nối AWS S3
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(Regions.AP_SOUTHEAST_1)
                    .build();

            InputStream inputStream = photo.getInputStream();
            ObjectMetadata metadata = new ObjectMetadata();

            // Xác định định dạng của file (JPEG hay PNG)
            String contentType = null;
            if (s3FileName != null) {
                if (s3FileName.endsWith(".png")) {
                    contentType = "image/png";
                } else if (s3FileName.endsWith(".jpg") || s3FileName.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                }
            }
            if (contentType == null) {
                throw new OurException("Only accept files with format JPG, JEPG or PNG");
            }
            metadata.setContentType(contentType);

            // Tạo yêu cầu lưu trữ file lên S3
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, s3FileName, inputStream, metadata);
            s3Client.putObject(putObjectRequest);

            // Trả về URL của file đã lưu trên S3
            return "https://" + bucketName + ".s3.amazonaws.com/" + s3FileName;
        } catch (Exception e) {
            e.printStackTrace();
            throw new OurException(e.getMessage());
        }
    }
}
