package com.internship.tool.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")
public class FileUploadController {

    // ✅ Upload folder
    private static final String UPLOAD_DIR =
            System.getProperty("user.dir")
                    + File.separator
                    + "uploads";

    // ✅ FILE UPLOAD
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> uploadFile(
            @RequestPart("file") MultipartFile file
    ) {

        try {

            // ✅ Create uploads folder
            File directory = new File(UPLOAD_DIR);

            if (!directory.exists()) {
                directory.mkdirs();
            }

            // ✅ Save file
            File savedFile = new File(
                    directory,
                    file.getOriginalFilename()
            );

            file.transferTo(savedFile);

            return ResponseEntity.ok(
                    "File uploaded successfully: "
                            + file.getOriginalFilename()
            );

        } catch (IOException e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body("File upload failed");
        }
    }

    // ✅ FILE DOWNLOAD
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileName
    ) {

        try {

            Path filePath =
                    Paths.get(UPLOAD_DIR)
                            .resolve(fileName)
                            .normalize();

            Resource resource =
                    new UrlResource(filePath.toUri());

            if (!resource.exists()) {

                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()

                    .contentType(MediaType.APPLICATION_OCTET_STREAM)

                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\""
                                    + resource.getFilename()
                                    + "\""
                    )

                    .body(resource);

        } catch (MalformedURLException e) {

            return ResponseEntity.internalServerError().build();
        }
    }
}
