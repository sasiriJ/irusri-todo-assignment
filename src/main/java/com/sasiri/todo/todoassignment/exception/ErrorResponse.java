package com.sasiri.todo.todoassignment.exception;

import lombok.AllArgsConstructor;

import java.util.Date;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private Date timestamp;
    private String message;
    private String details;
}