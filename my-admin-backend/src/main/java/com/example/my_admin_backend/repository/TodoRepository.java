package com.example.my_admin_backend.repository;

import com.example.my_admin_backend.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByUserIdAndDueDateOrderByCreatedAtDesc(Long userId, LocalDate dueDate);

    List<Todo> findByUserIdAndDueDateAndQuadrantOrderByCreatedAtDesc(Long userId, LocalDate dueDate, Integer quadrant);

    List<Todo> findByUserIdAndDueDateBetweenOrderByDueDateAscCreatedAtDesc(
            Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT t FROM Todo t WHERE t.userId = :userId AND " +
           "((t.dueDate >= :startDate AND t.dueDate <= :endDate) OR " +
           "(t.isCompleted = true AND t.completedAt >= :startDateTime AND t.completedAt <= :endDateTime)) " +
           "ORDER BY t.dueDate ASC, t.quadrant ASC")
    List<Todo> findByUserIdAndMonthRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startDateTime") java.time.LocalDateTime startDateTime,
            @Param("endDateTime") java.time.LocalDateTime endDateTime);

    List<Todo> findByUserId(Long userId);

    /** 获取用户所有未完成的待办（不限 dueDate），按创建时间倒序 */
    List<Todo> findByUserIdAndIsCompletedFalseOrderByCreatedAtDesc(Long userId);

    /** 获取所有已完成的待办，按完成时间倒序 */
    List<Todo> findByUserIdAndIsCompletedTrueOrderByCompletedAtDesc(Long userId);

    /**
     * 获取某天完成的所有待办（按 completedAt 落地的日期筛选）
     * 使用 [start, end) 半开区间，避免午夜的边界重复
     */
    @Query("SELECT t FROM Todo t WHERE t.userId = :userId AND t.isCompleted = true " +
           "AND t.completedAt >= :start AND t.completedAt < :end " +
           "ORDER BY t.completedAt DESC")
    List<Todo> findCompletedOnDate(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}