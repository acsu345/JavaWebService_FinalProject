# Implementation Guide: FR-03, FR-07, FR-08

## Overview
This document provides complete implementation details for three features of the badminton court booking system:

- **FR-03**: Logout & Revoke Token (JWT Blacklist)
- **FR-07**: View Booking History
- **FR-08**: Approve/Reject Booking (Admin/Manager)

## Tech Stack
- **Spring Boot**: 3.4.0
- **Java**: 21
- **Spring Security**: 6.x
- **JWT**: jjwt 0.11.2
- **JPA**: Spring Data JPA
- **Database**: MySQL
- **Build Tool**: Gradle
- **Lombok**: 1.18.30

## Architecture

```
Request
  ↓
Controller
  ↓
Service Interface
  ↓
Service Implementation
  ↓
Repository
  ↓
Database
```

---

## FR-03: Logout & Revoke Token

### Overview
Tokens are invalidated by adding them to a blacklist. When a user logs out, their JWT is stored in the `TokenBlacklist` table with its expiration time. The JWT filter checks this blacklist before allowing access.

### Components

#### 1. Entity: TokenBlacklist
**File**: `src/main/java/com/example/base_spring_boot/models/entities/TokenBlacklist.java`

```java
@Entity
@Table(name = "token_blacklist")
public class TokenBlacklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 512)
    private String token;
    
    @Column(nullable = false)
    private LocalDateTime expiredAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
```

#### 2. Repository: ITokenBlacklistRepository
**File**: `src/main/java/com/example/base_spring_boot/models/repositories/ITokenBlacklistRepository.java`

```java
@Repository
public interface ITokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    Optional<TokenBlacklist> findByToken(String token);
    boolean existsByToken(String token);
}
```

#### 3. Service Interface: IJwtBlacklistService
**File**: `src/main/java/com/example/base_spring_boot/models/services/IJwtBlacklistService.java`

```java
public interface IJwtBlacklistService {
    void blacklistToken(String token);
    boolean isTokenBlacklisted(String token);
}
```

#### 4. Service Implementation: JwtBlacklistServiceImpl
**File**: `src/main/java/com/example/base_spring_boot/models/services/impl/JwtBlacklistServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
public class JwtBlacklistServiceImpl implements IJwtBlacklistService {
    private final ITokenBlacklistRepository tokenBlacklistRepository;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public void blacklistToken(String token) {
        if (tokenBlacklistRepository.existsByToken(token)) {
            return;
        }

        Date expirationDate = jwtUtils.extractExpiration(token);
        LocalDateTime expiredAt = expirationDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        TokenBlacklist blacklist = TokenBlacklist.builder()
                .token(token)
                .expiredAt(expiredAt)
                .build();
        
        tokenBlacklistRepository.save(blacklist);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }
}
```

#### 5. Controller: AuthController
**File**: `src/main/java/com/example/base_spring_boot/controllers/AuthController.java`

```java
@PostMapping("/logout")
public ResponseEntity<?> handleLogout(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        jwtBlacklistService.blacklistToken(token);
    }
    return ResponseEntity.ok(
            DataRes.builder()
                    .status(HttpStatus.OK)
                    .code(200)
                    .message("Logout successfully")
                    .data(null)
                    .build()
    );
}
```

#### 6. JWT Filter: JwtTokenFilter
**File**: `src/main/java/com/example/base_spring_boot/security/jwt/JwtTokenFilter.java`

The filter checks if a token is blacklisted BEFORE processing:

```java
String token = getTokenFromRequest(request);
if (token != null) {
    if (jwtBlacklistService.isTokenBlacklisted(token)) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("Token is blacklisted");
        return;
    }
    // Continue with normal JWT processing...
}
```

#### 7. Exception Handler: HttpConflictException
**File**: `src/main/java/com/example/base_spring_boot/exceptions/HttpConflictException.java`

```java
public class HttpConflictException extends RuntimeException {
    public HttpConflictException(String message) {
        super(message);
    }
}
```

### API Endpoint

#### Logout
```
POST /api/v1/auth/logout
Authorization: Bearer {JWT_TOKEN}
```

**Response (200 OK)**:
```json
{
    "status": "OK",
    "code": 200,
    "message": "Logout successfully",
    "data": null
}
```

**Response (403 Forbidden - if token already blacklisted)**:
```json
{
    "status": "FORBIDDEN",
    "code": 403,
    "data": "Token is blacklisted"
}
```

---

## FR-07: View Booking History

### Overview
Customers can view their own booking history, sorted by creation date (newest first). Uses Java Stream API for data transformation as required.

### Components

#### 1. DTO: BookingHistoryResponse
**File**: `src/main/java/com/example/base_spring_boot/models/dtos/res/BookingHistoryResponse.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingHistoryResponse {
    private Long bookingId;
    private Long courtId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BookingStatus status;
}
```

#### 2. Service Interface: IBookingService
**File**: `src/main/java/com/example/base_spring_boot/models/services/IBookingService.java`

```java
public interface IBookingService {
    // ... other methods ...
    List<BookingHistoryResponse> getBookingHistory();
}
```

#### 3. Service Implementation: BookingServiceImpl
**File**: `src/main/java/com/example/base_spring_boot/models/services/impl/BookingServiceImpl.java`

```java
@Override
public List<BookingHistoryResponse> getBookingHistory() {
    User user = getCurrentUser();
    return bookingRepository.findAll().stream()
            .filter(booking -> booking.getUser().getId().equals(user.getId()))
            .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()))
            .map(booking -> BookingHistoryResponse.builder()
                    .bookingId(booking.getId())
                    .courtId(booking.getCourt().getId())
                    .bookingDate(booking.getBookingDate())
                    .startTime(booking.getSlot().getStartTime())
                    .endTime(booking.getSlot().getEndTime())
                    .status(booking.getStatus())
                    .build()
            )
            .collect(Collectors.toList());
}
```

#### 4. Controller: CustomerBookingController
**File**: `src/main/java/com/example/base_spring_boot/controllers/CustomerBookingController.java`

```java
@GetMapping("/history")
public ResponseEntity<?> getBookingHistory() {
    return ResponseEntity.ok(
            DataRes.builder()
                    .status(HttpStatus.OK)
                    .code(200)
                    .message("Booking history retrieved successfully")
                    .data(bookingService.getBookingHistory())
                    .build()
    );
}
```

### API Endpoint

#### Get Booking History
```
GET /api/v1/customer/bookings/history
Authorization: Bearer {JWT_TOKEN}
```

**Response (200 OK)**:
```json
{
    "status": "OK",
    "code": 200,
    "message": "Booking history retrieved successfully",
    "data": [
        {
            "bookingId": 1,
            "courtId": 5,
            "bookingDate": "2026-06-20",
            "startTime": "10:00:00",
            "endTime": "11:00:00",
            "status": "CONFIRMED"
        },
        {
            "bookingId": 2,
            "courtId": 3,
            "bookingDate": "2026-06-25",
            "startTime": "14:00:00",
            "endTime": "15:00:00",
            "status": "PENDING"
        }
    ]
}
```

**Response (401 Unauthorized)**:
```json
{
    "status": "UNAUTHORIZED",
    "code": 401,
    "data": "Unauthorized"
}
```

---

## FR-08: Approve/Reject Booking

### Overview
Only ADMIN and MANAGER roles can approve or reject pending bookings. The endpoint validates:
- Booking exists
- Booking is in PENDING status
- Requested status is CONFIRMED or REJECTED
- Updates the `updatedAt` timestamp

### Components

#### 1. DTO: BookingApprovalRequest
**File**: `src/main/java/com/example/base_spring_boot/models/dtos/req/BookingApprovalRequest.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingApprovalRequest {
    @NotNull(message = "status must not be null")
    private BookingStatus status;
}
```

#### 2. DTO: BookingApprovalResponse
**File**: `src/main/java/com/example/base_spring_boot/models/dtos/res/BookingApprovalResponse.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingApprovalResponse {
    private Long bookingId;
    private BookingStatus status;
    private LocalDateTime updatedAt;
}
```

#### 3. Service Interface: IBookingService
**File**: `src/main/java/com/example/base_spring_boot/models/services/IBookingService.java`

```java
public interface IBookingService {
    // ... other methods ...
    BookingApprovalResponse approveOrRejectBooking(Long bookingId, BookingApprovalRequest req);
}
```

#### 4. Service Implementation: BookingServiceImpl
**File**: `src/main/java/com/example/base_spring_boot/models/services/impl/BookingServiceImpl.java`

```java
@Override
@Transactional
public BookingApprovalResponse approveOrRejectBooking(Long bookingId, BookingApprovalRequest req) {
    Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new HttpNotFoundException("Booking not found"));

    // Check if booking is PENDING
    if (!booking.getStatus().equals(BookingStatus.PENDING)) {
        throw new HttpConflictException("Booking is not in PENDING status");
    }

    // Check if requested status is valid (only CONFIRMED or REJECTED)
    BookingStatus requestedStatus = req.getStatus();
    if (!requestedStatus.equals(BookingStatus.CONFIRMED) && !requestedStatus.equals(BookingStatus.REJECTED)) {
        throw new HttpBadRequestException("Invalid status. Only CONFIRMED or REJECTED are allowed");
    }

    // Update booking status
    booking.setStatus(requestedStatus);
    bookingRepository.save(booking);

    return BookingApprovalResponse.builder()
            .bookingId(booking.getId())
            .status(booking.getStatus())
            .updatedAt(booking.getUpdatedAt())
            .build();
}
```

#### 5. Controller: BookingAdminController
**File**: `src/main/java/com/example/base_spring_boot/controllers/BookingAdminController.java`

```java
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingAdminController {
    private final IBookingService bookingService;

    @PatchMapping("/{bookingId}/approval")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<?> approveOrRejectBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody BookingApprovalRequest req) {
        return ResponseEntity.ok(
                DataRes.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .message("Booking approved successfully")
                        .data(bookingService.approveOrRejectBooking(bookingId, req))
                        .build()
        );
    }
}
```

#### 6. Exception Handler: GlobalExceptionHandling
**File**: `src/main/java/com/example/base_spring_boot/advice/GlobalExceptionHandling.java`

Added handler for HttpConflictException:

```java
@ExceptionHandler(HttpConflictException.class)
public ResponseEntity<?> handleHttpConflict(HttpConflictException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(
            DataRes.builder()
                    .data(ex.getMessage())
                    .code(HttpStatus.CONFLICT.value())
                    .status(HttpStatus.CONFLICT)
                    .build()
    );
}
```

### API Endpoint

#### Approve/Reject Booking
```
PATCH /api/v1/bookings/{bookingId}/approval
Authorization: Bearer {ADMIN_OR_MANAGER_JWT_TOKEN}
Content-Type: application/json

Request Body:
{
    "status": "CONFIRMED"
}
```

or

```json
{
    "status": "REJECTED"
}
```

**Response (200 OK - Booking Confirmed)**:
```json
{
    "status": "OK",
    "code": 200,
    "message": "Booking approved successfully",
    "data": {
        "bookingId": 5,
        "status": "CONFIRMED",
        "updatedAt": "2026-06-12T15:30:45.123456"
    }
}
```

**Response (200 OK - Booking Rejected)**:
```json
{
    "status": "OK",
    "code": 200,
    "message": "Booking approved successfully",
    "data": {
        "bookingId": 5,
        "status": "REJECTED",
        "updatedAt": "2026-06-12T15:30:45.123456"
    }
}
```

**Response (404 Not Found)**:
```json
{
    "status": "NOT_FOUND",
    "code": 404,
    "data": "Booking not found"
}
```

**Response (409 Conflict - Already processed)**:
```json
{
    "status": "CONFLICT",
    "code": 409,
    "data": "Booking is not in PENDING status"
}
```

**Response (400 Bad Request - Invalid status)**:
```json
{
    "status": "BAD_REQUEST",
    "code": 400,
    "data": "Invalid status. Only CONFIRMED or REJECTED are allowed"
}
```

**Response (403 Forbidden - Not authorized)**:
```json
{
    "status": "FORBIDDEN",
    "code": 403,
    "data": "Access Denied"
}
```

---

## Security Configuration

### Updated SecurityConfig
**File**: `src/main/java/com/example/base_spring_boot/security/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    // ...
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // ... other config ...
                .authorizeHttpRequests(
                        url -> url
                                .requestMatchers("/api/v1/admin/**").hasAuthority(RoleName.ROLE_ADMIN.toString())
                                .requestMatchers("/api/v1/manager/**").hasAuthority(RoleName.ROLE_MANAGER.toString())
                                .requestMatchers("/api/v1/customer/**").hasAuthority(RoleName.ROLE_CUSTOMER.toString())
                                .requestMatchers("/api/v1/bookings/**").hasAnyAuthority(RoleName.ROLE_ADMIN.toString(), RoleName.ROLE_MANAGER.toString())
                                // Authorization Header endpoint with Bearer token
                                .anyRequest().permitAll()
                )
                // ... rest of config ...
                .build();
    }
}
```

**Role Requirements**:
- `/api/v1/auth/logout` - Any authenticated user
- `/api/v1/customer/bookings/history` - CUSTOMER role only
- `/api/v1/bookings/{id}/approval` - ADMIN or MANAGER role only

---

## Project Structure

```
base_spring_boot/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/base_spring_boot/
│   │   │       ├── advice/
│   │   │       │   └── GlobalExceptionHandling.java (UPDATED)
│   │   │       ├── controllers/
│   │   │       │   ├── AuthController.java (FR-03)
│   │   │       │   ├── BookingAdminController.java (NEW - FR-08)
│   │   │       │   └── CustomerBookingController.java (UPDATED - FR-07)
│   │   │       ├── exceptions/
│   │   │       │   ├── HttpBadRequestException.java
│   │   │       │   ├── HttpConflictException.java (NEW - FR-08)
│   │   │       │   └── HttpNotFoundException.java
│   │   │       ├── models/
│   │   │       │   ├── constants/
│   │   │       │   │   └── BookingStatus.java
│   │   │       │   ├── dtos/
│   │   │       │   │   ├── req/
│   │   │       │   │   │   ├── BookingApprovalRequest.java (EXISTING - FR-08)
│   │   │       │   │   │   └── BookingReq.java
│   │   │       │   │   ├── res/
│   │   │       │   │   │   ├── BookingApprovalResponse.java (EXISTING - FR-08)
│   │   │       │   │   │   ├── BookingHistoryResponse.java (EXISTING - FR-07)
│   │   │       │   │   │   └── BookingRes.java
│   │   │       │   │   └── wrapper/
│   │   │       │   │       └── DataRes.java (UPDATED)
│   │   │       │   ├── entities/
│   │   │       │   │   ├── Booking.java
│   │   │       │   │   ├── TokenBlacklist.java (EXISTING - FR-03)
│   │   │       │   │   ├── User.java
│   │   │       │   │   ├── Court.java
│   │   │       │   │   └── Slot.java
│   │   │       │   ├── repositories/
│   │   │       │   │   ├── IBookingRepository.java
│   │   │       │   │   ├── ITokenBlacklistRepository.java (EXISTING - FR-03)
│   │   │       │   │   └── IUserRepository.java
│   │   │       │   └── services/
│   │   │       │       ├── IBookingService.java (UPDATED)
│   │   │       │       ├── IJwtBlacklistService.java (EXISTING - FR-03)
│   │   │       │       └── impl/
│   │   │       │           ├── BookingServiceImpl.java (UPDATED)
│   │   │       │           └── JwtBlacklistServiceImpl.java (EXISTING - FR-03)
│   │   │       └── security/
│   │   │           ├── jwt/
│   │   │           │   ├── JwtTokenFilter.java (EXISTING - FR-03)
│   │   │           │   └── JwtUtils.java
│   │   │           └── SecurityConfig.java
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
└── build.gradle
```

---

## Error Handling

All errors follow a consistent format:

```json
{
    "status": "HTTP_STATUS",
    "code": HTTP_CODE,
    "data": "Error message"
}
```

### Status Codes

| Code | Status | Scenario |
|------|--------|----------|
| 200 | OK | Request successful |
| 400 | Bad Request | Invalid request data, invalid booking status |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Token is blacklisted, access denied |
| 404 | Not Found | Booking/user not found |
| 409 | Conflict | Booking already processed (not PENDING) |
| 500 | Internal Server Error | Server error |

---

## Testing

### Prerequisites
- MySQL database with `base_core` schema
- Admin/Manager user with valid JWT token
- Customer user with valid JWT token
- At least one booking in PENDING status

### Test Flow

1. **Test FR-03 (Logout)**:
   - Login to get JWT token
   - Call logout endpoint with token
   - Verify token is blacklisted
   - Try to use token again - should get 403 Forbidden

2. **Test FR-07 (History)**:
   - Login as customer
   - Call booking history endpoint
   - Verify response contains only customer's bookings
   - Verify bookings are sorted by createdAt DESC

3. **Test FR-08 (Approval)**:
   - Login as admin/manager
   - Get list of PENDING bookings
   - Call approval endpoint with bookingId and status
   - Verify booking status updated
   - Try to approve same booking again - should get 409 Conflict

---

## Dependencies Used

All dependencies are already in `build.gradle`:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-validation'
implementation 'io.jsonwebtoken:jjwt-api:0.11.2'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.2'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.2'
compileOnly 'org.projectlombok:lombok'
runtimeOnly 'com.mysql:mysql-connector-j'
```

---

## Database Schema

### TokenBlacklist Table
```sql
CREATE TABLE token_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(512) NOT NULL UNIQUE,
    expired_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_token (token),
    INDEX idx_expired_at (expired_at)
);
```

### Bookings Table (existing)
```sql
CREATE TABLE bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    court_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    booking_date DATE NOT NULL,
    total_price DOUBLE,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (court_id) REFERENCES courts(id),
    FOREIGN KEY (slot_id) REFERENCES slots(id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

---

## Compilation & Deployment

### Build Project
```bash
./gradlew clean build -x test
```

### Run Application
```bash
java -jar build/libs/base_spring_boot-0.0.1-SNAPSHOT.jar
```

Or directly with Gradle:
```bash
./gradlew bootRun
```

### Verify Build
```bash
./gradlew compileJava
```

The build output JAR: `build/libs/base_spring_boot-0.0.1-SNAPSHOT.jar`

---

## Summary

Three features have been successfully implemented:

✅ **FR-03**: JWT token blacklist for secure logout
✅ **FR-07**: Booking history with Stream API and sorting
✅ **FR-08**: Booking approval/rejection with role-based access

All code follows:
- Spring Boot best practices
- Clean architecture with separate layers
- Proper error handling
- Security best practices
- Transactional integrity
- SOLID principles


