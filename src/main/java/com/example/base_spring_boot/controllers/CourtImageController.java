package com.example.base_spring_boot.controllers;

import com.example.base_spring_boot.models.dtos.wrapper.DataRes;
import com.example.base_spring_boot.models.services.ICourtImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/court-images")
@RequiredArgsConstructor
@Slf4j
public class CourtImageController {
    private final ICourtImageService courtImageService;

    /**
     * Upload a single image to a court
     * Only MANAGER and ADMIN can upload images
     */
    @PostMapping("/upload/{courtId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long courtId,
            @RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(DataRes.builder()
                    .status(HttpStatus.OK)
                    .code(200)
                    .message("Image uploaded successfully")
                    .data(courtImageService.uploadImage(courtId, file))
                    .build());
        } catch (Exception e) {
            log.error("Error uploading image", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(DataRes.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .code(400)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Upload multiple images to a court
     * Only MANAGER and ADMIN can upload images
     */
    @PostMapping("/upload-multiple/{courtId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<?> uploadMultipleImages(
            @PathVariable Long courtId,
            @RequestParam("files") List<MultipartFile> files) {
        try {
            return ResponseEntity.ok(DataRes.builder()
                    .status(HttpStatus.OK)
                    .code(200)
                    .message("Images uploaded successfully")
                    .data(courtImageService.uploadMultipleImages(courtId, files))
                    .build());
        } catch (Exception e) {
            log.error("Error uploading multiple images", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(DataRes.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .code(400)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Get all images for a court
     * Available to all users
     */
    @GetMapping("/court/{courtId}")
    public ResponseEntity<?> getImages(@PathVariable Long courtId) {
        try {
            return ResponseEntity.ok(DataRes.builder()
                    .status(HttpStatus.OK)
                    .code(200)
                    .data(courtImageService.getImagesForCourt(courtId))
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving images", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(DataRes.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .code(404)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Delete an image
     * Only MANAGER and ADMIN can delete images
     */
    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<?> deleteImage(@PathVariable Long imageId) {
        try {
            courtImageService.deleteImage(imageId);
            return ResponseEntity.ok(DataRes.builder()
                    .status(HttpStatus.OK)
                    .code(200)
                    .message("Image deleted successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error deleting image", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(DataRes.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .code(404)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Update image display order
     * Only MANAGER and ADMIN can update images
     */
    @PutMapping("/{imageId}/order")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<?> updateImageOrder(
            @PathVariable Long imageId,
            @RequestParam Integer order) {
        try {
            courtImageService.updateImageOrder(imageId, order);
            return ResponseEntity.ok(DataRes.builder()
                    .status(HttpStatus.OK)
                    .code(200)
                    .message("Image order updated successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error updating image order", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(DataRes.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .code(400)
                            .message(e.getMessage())
                            .build());
        }
    }
}

