package com.example.my_admin_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "todos", indexes = {
    @Index(name = "idx_due_date", columnList = "dueDate"),
    @Index(name = "idx_quadrant", columnList = "quadrant"),
    @Index(name = "idx_user_id", columnList = "userId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private Integer quadrant;  // 1-4 对应四个象限

    @Column(nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @Column
    private LocalDateTime completedAt;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column
    private LocalDate plannedDate;  // 计划完成时间

    @Column(nullable = false)
    private Long userId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}