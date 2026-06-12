package com.example.base_spring_boot.models.services;

import com.example.base_spring_boot.models.dtos.req.CourtImageUrlReq;
import com.example.base_spring_boot.models.dtos.req.CourtImageUrlsReq;
import com.example.base_spring_boot.models.dtos.res.CourtImageRes;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ICourtImageService {
    /**
     * Upload a single image to a court
     *
     * @param courtId ID of the court
     * @param file image file to upload
     * @return the uploaded image response
     */
    CourtImageRes uploadImage(Long courtId, MultipartFile file);

    /**
     * Upload an image from a URL to a court
     *
     * @param courtId ID of the court
     * @param req request containing image URL
     * @return the uploaded image response
     */
    CourtImageRes uploadImageFromUrl(Long courtId, CourtImageUrlReq req);

    /**
     * Upload multiple images from URLs to a court
     *
     * @param courtId ID of the court
     * @param req request containing list of image URLs
     * @return list of uploaded image responses
     */
    List<CourtImageRes> uploadMultipleImagesFromUrls(Long courtId, CourtImageUrlsReq req);

    /**
     * Upload multiple images to a court
     *
     * @param courtId ID of the court
     * @param files list of image files to upload
     * @return list of uploaded image responses
     */
    List<CourtImageRes> uploadMultipleImages(Long courtId, List<MultipartFile> files);

    /**
     * Get all images for a court
     *
     * @param courtId ID of the court
     * @return list of image responses
     */
    List<CourtImageRes> getImagesForCourt(Long courtId);

    /**
     * Delete an image from a court
     *
     * @param imageId ID of the image to delete
     */
    void deleteImage(Long imageId);

    /**
     * Update image display order
     *
     * @param imageId ID of the image
     * @param displayOrder new display order
     */
    void updateImageOrder(Long imageId, Integer displayOrder);
}

