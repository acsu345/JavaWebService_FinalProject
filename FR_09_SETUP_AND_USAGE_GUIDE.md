# FR-09: Setup and Usage Guide - Court Image Upload Feature

## Overview
FR-09 enables managers to upload and manage multiple images for each court. Images are securely stored in Cloudinary and linked to the court through the database.

## Prerequisites
1. Cloudinary account (free tier available at https://cloudinary.com)
2. Spring Boot application running
3. Manager or Admin role in the system

## Setup Steps

### Step 1: Create Cloudinary Account
1. Visit https://cloudinary.com
2. Sign up for a free account
3. Navigate to Dashboard
4. Find your credentials:
   - Cloud Name
   - API Key
   - API Secret

### Step 2: Configure Environment Variables

#### On Windows (PowerShell):
```powershell
$env:CLOUDINARY_CLOUD_NAME="your_cloud_name"
$env:CLOUDINARY_API_KEY="your_api_key"
$env:CLOUDINARY_API_SECRET="your_api_secret"
```

#### On Linux/Mac:
```bash
export CLOUDINARY_CLOUD_NAME="your_cloud_name"
export CLOUDINARY_API_KEY="your_api_key"
export CLOUDINARY_API_SECRET="your_api_secret"
```

#### In IDE (IntelliJ IDEA):
1. Run → Edit Configurations
2. Add to Environment variables:
```
CLOUDINARY_CLOUD_NAME=your_cloud_name;CLOUDINARY_API_KEY=your_api_key;CLOUDINARY_API_SECRET=your_api_secret
```

#### In Application Properties:
Add to `application.yaml`:
```yaml
cloudinary:
  cloud-name: your_cloud_name
  api-key: your_api_key
  api-secret: your_api_secret
```

### Step 3: Start Application
```bash
cd D:\JavaWebService\base_spring_boot
./gradlew bootRun
```

The `court_images` table will be automatically created by Hibernate.

## API Usage Guide

### 1. Authenticate
First, get an authentication token using your Manager/Admin credentials:

**Request:**
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
    "username": "manager_username",
    "password": "manager_password"
}
```

**Response:**
```json
{
    "status": "OK",
    "code": 200,
    "data": {
        "token": "eyJhbGciOiJIUzUxMiJ9..."
    }
}
```

Save the token for subsequent requests.

### 2. Upload Single Image

**Using cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/court-images/upload/1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@C:\path\to\court_image.jpg"
```

**Using PowerShell:**
```powershell
$headers = @{
    "Authorization" = "Bearer YOUR_TOKEN"
}
$file = "C:\path\to\court_image.jpg"
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/court-images/upload/1" `
  -Method Post `
  -Headers $headers `
  -Form @{file = (Get-Item $file)}
```

**Response:**
```json
{
    "status": "OK",
    "code": 200,
    "message": "Image uploaded successfully",
    "data": {
        "id": 1,
        "imageUrl": "https://res.cloudinary.com/xxxxx/image/upload/...",
        "displayOrder": 1,
        "uploadedAt": "2025-06-12T10:30:00"
    }
}
```

### 3. Upload Multiple Images

**Using cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/court-images/upload-multiple/1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "files=@image1.jpg" \
  -F "files=@image2.jpg" \
  -F "files=@image3.jpg"
```

**Supported Formats:**
- Image files: jpg, jpeg, png, gif, webp
- Maximum file size: 5MB per file
- Maximum files per upload: 10
- Maximum total images per court: 50

### 4. Get All Images for a Court

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/court-images/court/1"
```

**Response:**
```json
{
    "status": "OK",
    "code": 200,
    "data": [
        {
            "id": 1,
            "imageUrl": "https://res.cloudinary.com/xxxxx/...",
            "displayOrder": 1,
            "uploadedAt": "2025-06-12T10:30:00"
        },
        {
            "id": 2,
            "imageUrl": "https://res.cloudinary.com/xxxxx/...",
            "displayOrder": 2,
            "uploadedAt": "2025-06-12T10:31:00"
        }
    ]
}
```

### 5. Update Image Display Order

Change the order in which images are displayed:

**Request:**
```bash
curl -X PUT "http://localhost:8080/api/v1/court-images/1/order?order=3" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response:**
```json
{
    "status": "OK",
    "code": 200,
    "message": "Image order updated successfully"
}
```

### 6. Delete an Image

**Request:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/court-images/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response:**
```json
{
    "status": "OK",
    "code": 200,
    "message": "Image deleted successfully"
}
```

Image will be deleted from both Cloudinary and the database.

### 7. Get Court Details with Images

The updated Court endpoint now includes images:

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/courts/1"
```

**Response:**
```json
{
    "status": "OK",
    "code": 200,
    "data": {
        "id": 1,
        "courtName": "Sân Số 1",
        "courtTypeName": "Sân Cầu Lông",
        "pricePerHour": 150000,
        "status": "AVAILABLE",
        "images": [
            {
                "id": 1,
                "imageUrl": "https://res.cloudinary.com/xxxxx/...",
                "displayOrder": 1,
                "uploadedAt": "2025-06-12T10:30:00"
            }
        ]
    }
}
```

## Using Postman

1. Import the provided `postman_court_images_collection.json` file
2. Set environment variables in Postman:
   - `base_url`: http://localhost:8080
   - `token`: Your JWT token from login
   - `courtId`: The court ID to upload images to
3. Use the collection to test all endpoints

## Error Handling

| Error | Status | Cause | Solution |
|-------|--------|-------|----------|
| "No beans of 'Cloudinary' type found" | 500 | Environment variables not set | Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET |
| "File size exceeds maximum limit" | 400 | File too large | Use images smaller than 5MB |
| "File type not allowed" | 400 | Wrong format | Use jpg, jpeg, png, gif, or webp |
| "Court not found" | 404 | Invalid courtId | Verify court exists |
| "Maximum 10 images per upload" | 400 | Too many files | Upload max 10 files per request |
| "Court cannot have more than 50 images" | 400 | Too many total images | Delete some images first |
| "Unauthorized" | 403 | Not Manager/Admin role | Use Manager or Admin account |
| "Unauthorized" (no token) | 401 | Missing token | Add Authorization header |

## Database Schema

The following table is automatically created:

```sql
CREATE TABLE court_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    court_id BIGINT NOT NULL,
    image_url LONGTEXT NOT NULL,
    public_id VARCHAR(255),
    uploaded_at DATETIME NOT NULL,
    display_order INT,
    FOREIGN KEY (court_id) REFERENCES courts(id) ON DELETE CASCADE,
    INDEX idx_court_id (court_id)
);
```

## Best Practices

1. **Optimize Images**: Compress images before upload to save bandwidth
2. **Consistent Sizes**: Use images of similar aspect ratios for better UI
3. **Fast Upload**: Order images logically (front, side, detail, etc.)
4. **Backup**: Keep local backups of important images
5. **Organization**: Use description or naming conventions to track images

## Troubleshooting

### Images not uploading?
- Check Cloudinary credentials are correct
- Verify file format is supported
- Ensure file is not corrupted
- Check file size (max 5MB)

### Slow uploads?
- Check internet connection
- Compress images before upload
- Upload during off-peak hours
- Split into smaller batches

### Images not showing in API?
- Verify court exists (GET /api/v1/courts/{id})
- Check if images are associated with correct court
- Clear browser cache if using web interface

### Permission denied?
- Verify user has MANAGER or ADMIN role
- Check token is valid and not expired
- Use correct Authorization header format: `Bearer YOUR_TOKEN`

## Advanced Features

### Bulk Operations
To upload multiple sets of images:
```bash
for i in 1..5
{
    curl -X POST "http://localhost:8080/api/v1/court-images/upload/1" `
      -H "Authorization: Bearer YOUR_TOKEN" `
      -F "file=@image$i.jpg"
}
```

### Filter and Sort
The images are automatically sorted by display order when retrieved.

### Bulk Delete
Delete all images for a court:
```bash
# Get all images
GET /api/v1/court-images/court/{courtId}

# Delete each image
DELETE /api/v1/court-images/{imageId}
```

## Security Considerations

1. **Authentication**: Only logged-in users can see images
2. **Authorization**: Only MANAGER/ADMIN can upload/delete
3. **File Validation**: Strict type and size checks
4. **Cloudinary Security**: Uses API credentials for server-side operations
5. **Automatic Cleanup**: Images deleted from Cloudinary when court is deleted

## Performance

- Image metadata is cached in database
- Cloudinary handles image optimization
- Display order allows efficient sorting on frontend
- Lazy loading of images recommended for many courts

## Support Files

- **Documentation**: `FR_09_IMAGE_UPLOAD_FEATURE.md` - Full technical documentation
- **Postman Collection**: `postman_court_images_collection.json` - Ready-to-use API tests
- **This Guide**: `FR_09_SETUP_AND_USAGE_GUIDE.md` - Setup and usage instructions

## Next Steps

1. ✅ Environment setup complete
2. ✅ Application running
3. Upload your first court image
4. Integrate with frontend UI
5. Monitor uploads in Cloudinary dashboard

For more technical details, see `FR_09_IMAGE_UPLOAD_FEATURE.md`

