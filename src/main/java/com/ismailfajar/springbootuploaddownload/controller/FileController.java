package com.ismailfajar.springbootuploaddownload.controller;

import com.ismailfajar.springbootuploaddownload.model.FileResponse;
import com.ismailfajar.springbootuploaddownload.service.IFileSystemStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class FileController {

    @Autowired
    private IFileSystemStorage fileSystemStorage;

    @PostMapping("/uploadfile")
    public ResponseEntity<FileResponse> uploadSingleFile(@RequestParam("file") MultipartFile file) {
        String upfile = fileSystemStorage.saveFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/download/")
                .path(upfile)
                .toUriString();

        return ResponseEntity.status(HttpStatus.OK).body(new FileResponse(upfile, fileDownloadUri, "File uploaded " +
                "with success!"));
    }

    @PostMapping("/uploadfiles")
    public ResponseEntity<List<FileResponse>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        List<FileResponse> responses = Arrays.asList(files)
                .stream()
                .map(
                        file -> {
                            String upfile = fileSystemStorage.saveFile(file);
                            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                                    .path("/api/download/")
                                    .path(upfile)
                                    .toUriString();
                            return new FileResponse(upfile, fileDownloadUri, "File uploaded with success!");
                        }
                )
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {

        Resource resource = fileSystemStorage.loadFile(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
