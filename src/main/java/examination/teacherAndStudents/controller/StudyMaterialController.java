package examination.teacherAndStudents.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import examination.teacherAndStudents.entity.StudyMaterial;
import examination.teacherAndStudents.service.serviceImpl.StudyMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    @Value("${google.drive.folder.id}")
    private String googleDriveFolderId;


    private final StudyMaterialService studyMaterialService;

    @GetMapping
    public ResponseEntity<List<StudyMaterial>> getAllMaterials() {
        List<StudyMaterial> materials = studyMaterialService.getAllMaterials();
        return new ResponseEntity<>(materials, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyMaterial> getMaterialById(@PathVariable Long id) {
        StudyMaterial material = studyMaterialService.getMaterialById(id);
        if (material != null) {
            return new ResponseEntity<>(material, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/material")
    public ResponseEntity<StudyMaterial> uploadStudyMaterial(@RequestParam("file") MultipartFile file,
                                                        @RequestParam("title") String title) {
//        "/Users/mac/Documents/ResultStatement.pdf"
        String filePath = "/Users/mac/Documents/" + file.getOriginalFilename();

        StudyMaterial material = new StudyMaterial();
        material.setTitle(title);
        material.setFilePath(filePath);

        studyMaterialService.saveMaterial(material);

        return new ResponseEntity<>(material, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        StudyMaterial material = studyMaterialService.getMaterialById(id);
        if (material != null) {
            studyMaterialService.deleteMaterial(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadMaterial(@PathVariable Long id) {
        StudyMaterial material = studyMaterialService.getMaterialById(id);

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



        @PostMapping("/upload")
        public ResponseEntity<String> uploadMaterial(@RequestParam("file") MultipartFile file,
                                                     @RequestParam("title") String title) {

            try {
                Drive driveService = getDriveService();

                // Create file metadata
                File fileMetadata = new File();
                fileMetadata.setName(file.getOriginalFilename());
                fileMetadata.setParents(Collections.singletonList(googleDriveFolderId)); // Set the folder ID in Google Drive

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

            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
                return new ResponseEntity<>("Failed to upload file to Google Drive", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        private Drive getDriveService() throws IOException, GeneralSecurityException {
            // Load the credentials JSON file you downloaded from Google Cloud Console
            // Note: Replace "/path/to/your/credentials.json" with the actual path or use InputStream
            java.io.File credentialsFile = new java.io.File("/path/to/your/credentials.json");
            GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(credentialsFile))
                    .createScoped(Collections.singletonList(DriveScopes.DRIVE_FILE));
            return new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential)
                    .setApplicationName("Your Application Name")
                    .build();
        }




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



