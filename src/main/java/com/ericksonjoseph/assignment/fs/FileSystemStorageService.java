package com.ericksonjoseph.assignment.fs;

import com.ericksonjoseph.assignment.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;
import java.util.Date;

@Service
public class FileSystemStorageService implements StorageService {

    private final long chunkSizeBytes = 4194304;
    private final Path rootLocation;

    private final AmazonS3 s3Client = new AmazonS3Client(new ProfileCredentialsProvider());
    private static String existingBucketName  = Config.get("aws.s3.backup.bucket");
    private static String key                 = Config.get("aws.s3.backup.key");

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public String store(MultipartFile file) {

        if (file.isEmpty()) {
            throw new StorageException("Empty file " + file.getOriginalFilename());
        }

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String filename = file.getOriginalFilename() + "-" + timestamp.getTime();

        try {
            Files.copy(file.getInputStream(), this.rootLocation.resolve(filename));
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }

        return this.rootLocation.resolve(filename).toString();
    }

    @Override
    public void backup(String filePath) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        // Generate a key name to be used in S3
        String keyName = key + "-" + timestamp.getTime();

        // List of UploadPartResponse objects
        List<PartETag> partETags = new ArrayList<PartETag>();

        // Setup S3 Upload
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(
                existingBucketName, keyName);
        InitiateMultipartUploadResult initResponse = 
            s3Client.initiateMultipartUpload(initRequest);

        File file = new File(filePath);
        long contentLength = file.length();
        long chunkSize = chunkSizeBytes;
        long filePosition = 0;

        try {
            for (int i = 1; filePosition < contentLength; i++) {
                chunkSize = Math.min(chunkSize, (contentLength - filePosition));

                UploadPartRequest uploadRequest = new UploadPartRequest()
                    .withBucketName(existingBucketName).withKey(keyName)
                    .withUploadId(initResponse.getUploadId()).withPartNumber(i)
                    .withFileOffset(filePosition)
                    .withFile(file)
                    .withPartSize(chunkSize);

                partETags.add(s3Client.uploadPart(uploadRequest).getPartETag());

                filePosition += chunkSize;
            }

            CompleteMultipartUploadRequest compRequest = new 
                CompleteMultipartUploadRequest(existingBucketName, 
                        keyName, 
                        initResponse.getUploadId(), 
                        partETags);

            s3Client.completeMultipartUpload(compRequest);
        } catch (Exception e) {
            s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(
                        existingBucketName, keyName, initResponse.getUploadId()));
            throw new StorageException("Failed to upload file to S3", e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(path -> this.rootLocation.relativize(path));
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectory(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize file system", e);
        }
    }
}
