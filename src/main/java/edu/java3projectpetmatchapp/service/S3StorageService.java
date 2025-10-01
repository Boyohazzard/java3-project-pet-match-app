package edu.java3projectpetmatchapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class S3StorageService {

    // From application.properties
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region}")
    private String awsRegion;

    // This must match the file name of the photo that was uploaded.
    private static final String DEFAULT_PHOTO_KEY = "profile_default.png";

    // Constructs a permanent public URL for the default profile photo.
     public String getDefaultProfilePhotoUrl() {
        // Standard S3 public URL format for public objects:
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                awsRegion,
                DEFAULT_PHOTO_KEY);
    }
}