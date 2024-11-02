package com.sasiri.todo.todoassignment.repository;

import com.sasiri.todo.todoassignment.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    Page<Todo> findByUserId(Long userId, Pageable pageable);
    
    Page<Todo> findByUserIdAndTitleContaining(Long userId, String title, Pageable pageable);
    
    Optional<Todo> findByIdAndUserId(Long id, Long userId);
    
    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId " +
           "AND (:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:completed IS NULL OR t.completed = :completed) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "ORDER BY CASE WHEN :sortBy = 'dueDate' THEN t.dueDate END ASC, " +
           "CASE WHEN :sortBy = 'priority' THEN t.priority END DESC")
    Page<Todo> findWithFilters(
        @Param("userId") Long userId,
        @Param("title") String title,
        @Param("completed") Boolean completed,
        @Param("priority") Integer priority,
        @Param("sortBy") String sortBy,
        Pageable pageable
    );
    
    List<Todo> findByUserIdAndDueDateBefore(Long userId, LocalDateTime date);
    
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.user.id = :userId AND t.completed = true")
    long countCompletedByUser(@Param("userId") Long userId);
}