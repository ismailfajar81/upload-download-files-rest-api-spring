package com.ismailfajar.springbootuploaddownload.service;

import com.ismailfajar.springbootuploaddownload.exception.FileStorageException;
import com.ismailfajar.springbootuploaddownload.properties.FileUploadProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileSystemStorageService implements IFileSystemStorage{

    private final Path dirlocation;

    @Autowired
    public FileSystemStorageService(FileUploadProperties fileUploadProperties) {
        this.dirlocation = Paths.get(fileUploadProperties.getLocation())
                .toAbsolutePath()
                .normalize();
    }

    @Override
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.dirlocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create upload dir");
        }
    }

    @Override
    public String saveFile(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            Path dfile = this.dirlocation.resolve(fileName);
            Files.copy(file.getInputStream(), dfile, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (Exception e) {
            throw new FileStorageException("Could not upload file");
        }
    }

    @Override
    public Resource loadFile(String fileName) {
        try {
            Path file = this.dirlocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("Could not find file");
            }
            return null;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not find download file");
        }
    }

}
