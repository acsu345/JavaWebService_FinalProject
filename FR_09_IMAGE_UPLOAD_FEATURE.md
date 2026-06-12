# FR-09: Upload and Store Multiple Court Images

## Feature Overview
This feature allows managers to upload and manage multiple images for each court. Images are stored on Cloudinary (cloud storage service) and linked to courts in the database.

## Implementation Details

### Database Schema

#### New Table: `court_images`
```sql
CREATE TABLE court_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    court_id BIGINT NOT NULL,
    image_url LONGTEXT NOT NULL,
    public_id VARCHAR(255),
    uploaded_at DATETIME,
    display_order INT,
    FOREIGN KEY (court_id) REFERENCES courts(id) ON DELETE CASCADE
);
```

### New Entities

#### 1. CourtImage Entity
Located at: `src/main/java/com/example/base_spring_boot/models/entities/CourtImage.java`

Fields:
- `id` - Primary key
- `court` - Reference to Court entity (ManyToOne relationship)
- `imageUrl` - URL from Cloudinary
- `publicId` - Cloudinary public ID (for deletion)
- `uploadedAt` - Timestamp of upload
- `displayOrder` - Order for displaying images

#### 2. Updated Court Entity
Added OneToMany relationship to CourtImage:
```java
@OneToMany(mappedBy = "court", cascade = CascadeType.ALL, orphanRemoval = true)
private List<CourtImage> images = new ArrayList<>();
```

### Services

#### ICourtImageService Interface
Located at: `src/main/java/com/example/base_spring_boot/models/services/ICourtImageService.java`

Methods:
- `uploadImage(Long courtId, MultipartFile file)` - Upload single image
- `uploadMultipleImages(Long courtId, List<MultipartFile> files)` - Upload multiple images (max 10 at once)
- `getImagesForCourt(Long courtId)` - Retrieve all images for a court
- `deleteImage(Long imageId)` - Delete an image
- `updateImageOrder(Long imageId, Integer displayOrder)` - Change image display order

#### CourtImageServiceImpl Implementation
Located at: `src/main/java/com/example/base_spring_boot/models/services/impl/CourtImageServiceImpl.java`

Features:
- File validation (size: max 5MB, types: jpg, jpeg, png, gif, webp)
- Maximum 10 images per upload request
- Maximum 50 images per court
- Automatic folder organization in Cloudinary: `court_images/court_{courtId}`
- Automatic timestamp generation on upload
- Proper error handling and logging

### API Endpoints

#### 1. Upload Single Image
```
POST /api/v1/court-images/upload/{courtId}
Content-Type: multipart/form-data

Authorization: Bearer {token}
Required Role: MANAGER or ADMIN

Request: 
- file: MultipartFile (5MB max)

Response:
{
    "status": "OK",
    "code": 200,
    "message": "Image uploaded successfully",
    "data": {
        "id": 1,
        "imageUrl": "https://res.cloudinary.com/...",
        "displayOrder": 1,
        "uploadedAt": "2025-06-12T10:30:00"
    }
}
```

#### 2. Upload Multiple Images
```
POST /api/v1/court-images/upload-multiple/{courtId}
Content-Type: multipart/form-data

Authorization: Bearer {token}
Required Role: MANAGER or ADMIN

Request:
- files: MultipartFile[] (max 10 files, 5MB each)

Response:
{
    "status": "OK",
    "code": 200,
    "message": "Images uploaded successfully",
    "data": [
        {
            "id": 1,
            "imageUrl": "https://res.cloudinary.com/...",
            "displayOrder": 1,
            "uploadedAt": "2025-06-12T10:30:00"
        },
        ...
    ]
}
```

#### 3. Get Court Images
```
GET /api/v1/court-images/court/{courtId}

Response:
{
    "status": "OK",
    "code": 200,
    "data": [
        {
            "id": 1,
            "imageUrl": "https://res.cloudinary.com/...",
            "displayOrder": 1,
            "uploadedAt": "2025-06-12T10:30:00"
        },
        ...
    ]
}
```

#### 4. Delete Image
```
DELETE /api/v1/court-images/{imageId}

Authorization: Bearer {token}
Required Role: MANAGER or ADMIN

Response:
{
    "status": "OK",
    "code": 200,
    "message": "Image deleted successfully"
}
```

#### 5. Update Image Display Order
```
PUT /api/v1/court-images/{imageId}/order?order={displayOrder}

Authorization: Bearer {token}
Required Role: MANAGER or ADMIN

Response:
{
    "status": "OK",
    "code": 200,
    "message": "Image order updated successfully"
}
```

### Configuration

#### Environment Variables Required
Add to your `.env` or system environment:
```
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

#### application.yaml Configuration
```yaml
cloudinary:
  cloud-name: ${CLOUDINARY_CLOUD_NAME}
  api-key: ${CLOUDINARY_API_KEY}
  api-secret: ${CLOUDINARY_API_SECRET}

spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 100MB
```

### DTOs

#### CourtImageRes (Response DTO)
```java
{
    "id": Long,
    "imageUrl": String,
    "displayOrder": Integer,
    "uploadedAt": LocalDateTime
}
```

#### Updated CourtRes
Now includes:
```java
{
    "id": Long,
    "courtName": String,
    "courtTypeName": String,
    "pricePerHour": Double,
    "status": String,
    "images": List<CourtImageRes>  // New field
}
```

### Validation Rules

1. **File Size**: Maximum 5MB per file
2. **File Types**: jpg, jpeg, png, gif, webp only
3. **Batch Upload Limit**: Maximum 10 files per upload request
4. **Total Images per Court**: Maximum 50 images
5. **Display Order**: Must be a positive number

### Directory Structure

```
src/main/java/com/example/base_spring_boot/
├── models/
│   ├── entities/
│   │   ├── Court.java (updated)
│   │   └── CourtImage.java (new)
│   ├── repositories/
│   │   └── ICourtImageRepository.java (new)
│   ├── services/
│   │   ├── ICourtImageService.java (new)
│   │   └── impl/
│   │       ├── CourtImageServiceImpl.java (new)
│   │       └── CourtServiceImpl.java (updated)
│   └── dtos/
│       └── res/
│           ├── CourtRes.java (updated)
│           └── CourtImageRes.java (new)
├── controllers/
│   └── CourtImageController.java (new)
└── config/
    └── CloudinaryConfig.java (new)
```

### Error Handling

The implementation includes comprehensive error handling:

| Status Code | Scenario |
|-------------|----------|
| 200 | Successful operation |
| 400 | Invalid file (size, type, empty), max upload exceeded |
| 404 | Court not found, Image not found |
| 403 | Insufficient permissions (not MANAGER or ADMIN) |

### Security Features

1. **Role-Based Access Control**: Only MANAGER and ADMIN can upload/delete images
2. **File Validation**: Strict file type and size validation
3. **Cloudinary Integration**: Secure image storage with automatic cleanup
4. **Database Cascade**: Images are automatically deleted when court is deleted

### Dependencies

The project already includes:
```gradle
implementation("com.cloudinary:cloudinary-core:1.38.0")
implementation("com.cloudinary:cloudinary-http44:1.39.0")
```

### Usage Example (cURL)

Upload single image:
```bash
curl -X POST "http://localhost:8080/api/v1/court-images/upload/1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@/path/to/image.jpg"
```

Upload multiple images:
```bash
curl -X POST "http://localhost:8080/api/v1/court-images/upload-multiple/1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "files=@/path/to/image1.jpg" \
  -F "files=@/path/to/image2.jpg" \
  -F "files=@/path/to/image3.jpg"
```

Get court images:
```bash
curl -X GET "http://localhost:8080/api/v1/court-images/court/1"
```

Delete image:
```bash
curl -X DELETE "http://localhost:8080/api/v1/court-images/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

Update image order:
```bash
curl -X PUT "http://localhost:8080/api/v1/court-images/1/order?order=2" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Next Steps

1. Set up Cloudinary account and get credentials
2. Add environment variables to your system or IDE
3. Run the application (Hibernate will auto-create the `court_images` table)
4. Test the endpoints using Postman or curl
5. Integrate frontend UI for image upload

### Notes

- Images are stored in Cloudinary (not in the local file system)
- Each court can have up to 50 images
- Images are organized by court ID in Cloudinary for easy management
- When a court is deleted, all associated images are also deleted from both database and Cloudinary
- Display order can be used by the frontend to arrange images

