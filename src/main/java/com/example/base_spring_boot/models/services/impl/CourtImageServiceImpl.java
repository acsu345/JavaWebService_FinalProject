package com.example.base_spring_boot.models.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.base_spring_boot.exceptions.HttpBadRequestException;
import com.example.base_spring_boot.exceptions.HttpNotFoundException;
import com.example.base_spring_boot.models.dtos.res.CourtImageRes;
import com.example.base_spring_boot.models.entities.Court;
import com.example.base_spring_boot.models.entities.CourtImage;
import com.example.base_spring_boot.models.repositories.ICourtImageRepository;
import com.example.base_spring_boot.models.repositories.ICourtRepository;
import com.example.base_spring_boot.models.services.ICourtImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourtImageServiceImpl implements ICourtImageService {
    private final ICourtImageRepository courtImageRepository;
    private final ICourtRepository courtRepository;
    private final Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};

    @Override
    public CourtImageRes uploadImage(Long courtId, MultipartFile file) {
        List<MultipartFile> files = List.of(file);
        return uploadMultipleImages(courtId, files).stream().findFirst()
                .orElseThrow(() -> new HttpBadRequestException("Failed to upload image"));
    }

    @Override
    public List<CourtImageRes> uploadMultipleImages(Long courtId, List<MultipartFile> files) {
        // Verify court exists
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new HttpNotFoundException("Court not found with id: " + courtId));

        // Validate files
        if (files == null || files.isEmpty()) {
            throw new HttpBadRequestException("No files provided");
        }

        if (files.size() > 10) {
            throw new HttpBadRequestException("Maximum 10 images per upload");
        }

        // Get current image count for this court
        int currentImageCount = courtImageRepository.countByCourtId(courtId);
        if (currentImageCount + files.size() > 50) {
            throw new HttpBadRequestException("Court cannot have more than 50 images total");
        }

        List<CourtImageRes> uploadedImages = files.stream()
                .map(file -> uploadSingleImage(court, file, currentImageCount + files.indexOf(file)))
                .collect(Collectors.toList());

        return uploadedImages;
    }

    private CourtImageRes uploadSingleImage(Court court, MultipartFile file, int index) {
        try {
            // Validate file
            validateFile(file);

            // Upload to Cloudinary
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "court_images/court_" + court.getId(),
                            "resource_type", "auto",
                            "public_id", "image_" + System.currentTimeMillis()
                    ));

            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            // Save to database
            CourtImage courtImage = CourtImage.builder()
                    .court(court)
                    .imageUrl(imageUrl)
                    .publicId(publicId)
                    .displayOrder(index + 1)
                    .build();

            courtImage = courtImageRepository.save(courtImage);
            court.getImages().add(courtImage);

            log.info("Successfully uploaded image for court: {}", court.getId());

            return mapToCourtImageRes(courtImage);
        } catch (IOException e) {
            log.error("Error uploading image to Cloudinary", e);
            throw new HttpBadRequestException("Failed to upload image: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new HttpBadRequestException("File is empty");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new HttpBadRequestException("File size exceeds maximum limit of 5MB");
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isAllowedExtension(originalFilename)) {
            throw new HttpBadRequestException("File type not allowed. Allowed types: jpg, jpeg, png, gif, webp");
        }
    }

    private boolean isAllowedExtension(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<CourtImageRes> getImagesForCourt(Long courtId) {
        // Verify court exists
        courtRepository.findById(courtId)
                .orElseThrow(() -> new HttpNotFoundException("Court not found with id: " + courtId));

        return courtImageRepository.findByCourtIdOrderByDisplayOrder(courtId).stream()
                .map(this::mapToCourtImageRes)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteImage(Long imageId) {
        CourtImage image = courtImageRepository.findById(imageId)
                .orElseThrow(() -> new HttpNotFoundException("Image not found with id: " + imageId));

        try {
            // Delete from Cloudinary
            if (image.getPublicId() != null && !image.getPublicId().isEmpty()) {
                cloudinary.uploader().destroy(image.getPublicId(), ObjectUtils.emptyMap());
                log.info("Successfully deleted image from Cloudinary: {}", image.getPublicId());
            }
        } catch (Exception e) {
            log.warn("Failed to delete image from Cloudinary, but will proceed with database deletion", e);
        }

        // Delete from database
        courtImageRepository.deleteById(imageId);
        log.info("Successfully deleted image from database: {}", imageId);
    }

    @Override
    public void updateImageOrder(Long imageId, Integer displayOrder) {
        CourtImage image = courtImageRepository.findById(imageId)
                .orElseThrow(() -> new HttpNotFoundException("Image not found with id: " + imageId));

        if (displayOrder == null || displayOrder < 1) {
            throw new HttpBadRequestException("Display order must be a positive number");
        }

        image.setDisplayOrder(displayOrder);
        courtImageRepository.save(image);
        log.info("Successfully updated image order for image: {}", imageId);
    }

    private CourtImageRes mapToCourtImageRes(CourtImage courtImage) {
        return CourtImageRes.builder()
                .id(courtImage.getId())
                .imageUrl(courtImage.getImageUrl())
                .displayOrder(courtImage.getDisplayOrder())
                .uploadedAt(courtImage.getUploadedAt())
                .build();
    }
}

