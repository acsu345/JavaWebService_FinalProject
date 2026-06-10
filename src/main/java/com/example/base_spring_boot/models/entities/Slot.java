package com.example.base_spring_boot.models.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "slots")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Slot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;
    
    // Bạn có thể thêm giá riêng cho từng slot nếu cần (ví dụ giờ cao điểm)
}
