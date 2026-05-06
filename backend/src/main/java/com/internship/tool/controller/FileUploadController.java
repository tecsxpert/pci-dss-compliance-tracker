package com.internship.tool.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FileUploadController {

    // ✅ Absolute uploads folder
    private static final String UPLOAD_DIR =
            System.getProperty("user.dir")
                    + File.separator
                    + "uploads";

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

            // ✅ Full file path
            File savedFile = new File(
                    directory,
                    file.getOriginalFilename()
            );

            // ✅ Save file
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
}