package com.tiqs.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Enrollment {
    private Long id;
    private Long classId;
    private Long studentId;
    private LocalDateTime joinedAt;
}
