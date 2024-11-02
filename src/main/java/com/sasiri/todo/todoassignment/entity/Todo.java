package com.sasiri.todo.todoassignment.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "todos")
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String description;
    private boolean completed;
    private LocalDateTime dueDate;
    private int priority;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}