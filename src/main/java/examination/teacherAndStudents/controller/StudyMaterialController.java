package examination.teacherAndStudents.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import examination.teacherAndStudents.dto.StudyMaterialRequest;
import examination.teacherAndStudents.dto.StudyMaterialResponse;
import examination.teacherAndStudents.entity.StudyMaterial;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.service.StudyMaterialService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/materials")
public class StudyMaterialController {
//    @Value("${google.drive.folder.id}")
//    private String googleDriveFolderId;
//
//    @Value("${google.credentials.path}")
//    private String googleCredentialsPath;


    private final StudyMaterialService studyMaterialService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT', 'ADMIN')")
    public ResponseEntity<Page<StudyMaterialResponse>> getAllMaterials(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @Positive int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        try {
            Page<StudyMaterialResponse> materials = studyMaterialService.getAllMaterials(page, size, sortBy, sortDirection);
            return ResponseEntity.ok(materials);
        } catch (CustomNotFoundException e) {
            throw e; // Handled by global exception handler
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid request parameters: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomNotFoundException("Failed to retrieve study materials: " + e.getMessage());
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<StudyMaterialResponse> getMaterialById(@PathVariable Long id) {
        StudyMaterialResponse material = studyMaterialService.getMaterialById(id);
        if (material != null) {
            return new ResponseEntity<>(material, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/material")
    public ResponseEntity<StudyMaterialResponse> uploadStudyMaterial(@RequestParam("file") MultipartFile file,
                                                        @RequestParam("title") String title) {
//        "/Users/mac/Documents/ResultStatement.pdf"
        String filePath = "/Users/mac/Documents/" + file.getOriginalFilename();

        StudyMaterialRequest material = new StudyMaterialRequest();
        material.setTitle(title);
        material.setFilePath(filePath);

        StudyMaterialResponse response =     studyMaterialService.saveMaterial(material);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        StudyMaterialResponse material = studyMaterialService.getMaterialById(id);
        if (material != null) {
            studyMaterialService.deleteMaterial(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadMaterial(@PathVariable Long id) {
        StudyMaterialResponse material = studyMaterialService.getMaterialById(id);

        if (material != null) {
            // Load the file as a resource
            Resource resource = loadFileAsResource(material.getFilePath());

            // Return the file as a ResponseEntity
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Helper method to load file as a Resource
    private Resource loadFileAsResource(String filePath) {
        try {
            Path file = Paths.get(filePath);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Malformed URL!");
        }
    }



        @PostMapping("/upload/google")
        public ResponseEntity<String> uploadMaterial(@RequestParam("file") MultipartFile file,
                                                     @RequestParam("title") String title) {

            try {
//                Drive driveService = getDriveService();

                Drive driveService = null;

                // Create file metadata
                File fileMetadata = new File();
                fileMetadata.setName(file.getOriginalFilename());
//                fileMetadata.setParents(Collections.singletonList(googleDriveFolderId)); // Set the folder ID in Google Drive

                // Convert MultipartFile to java.io.File
                java.io.File tempFile = java.io.File.createTempFile("temp", null);
                file.transferTo(tempFile);

                // Create media content
                FileContent mediaContent = new FileContent("application/pdf", tempFile);

                // Upload the file to Google Drive
                File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();

                // You can save the file ID or other details to your database if needed
                String fileId = uploadedFile.getId();

                // Clean up temporary file
                tempFile.delete();

                return new ResponseEntity<>("File uploaded to Google Drive with ID: " + fileId, HttpStatus.CREATED);

            } catch (Exception  e) {
                e.printStackTrace();
                return new ResponseEntity<>("Failed to upload file to Google Drive", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

//    private Drive getDriveService() throws IOException, GeneralSecurityException {
//        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(googleCredentialsPath))
//                .createScoped(Collections.singletonList(DriveScopes.DRIVE_FILE));
//
//        return new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(),
//                JacksonFactory.getDefaultInstance(), credential)
//                .setApplicationName("Your Application Name")
//                .build();
//    }




    @GetMapping("/download/{fileId}")
    public ResponseEntity<InputStreamResource> downloadMaterial(@PathVariable String fileId) {

        // Assume you have a service to retrieve the file path based on fileId
        // Here, we're using a placeholder method getFilePathForFileId
        String filePath = getFilePathForFileId(fileId);

        try {
            InputStream inputStream = new FileInputStream(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "downloaded_file.pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);

            return new ResponseEntity<>(new InputStreamResource(inputStream), headers, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Placeholder method - Replace this with your logic to retrieve file path
    private String getFilePathForFileId(String fileId) {
        // Your logic to retrieve the file path based on fileId
        // For simplicity, returning a placeholder path here
        return "/path/to/your/files/" + fileId + ".pdf";
    }
    }



