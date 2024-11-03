package com.sasiri.todo.todoassignment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> resourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse errorDetails = new ErrorResponse(
            new Date(),
            ex.getMessage(),
            request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> badRequestException(
            BadRequestException ex, WebRequest request) {
        ErrorResponse errorDetails = new ErrorResponse(
            new Date(),
            ex.getMessage(),
            request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        ErrorResponse errorDetails = new ErrorResponse(
            new Date(),
            "Invalid username or password",
            request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handelUserAlreadyException(
            UserAlreadyExistsException ex, WebRequest request) {
        ErrorResponse errorDetails = new ErrorResponse(
                new Date(),
                ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<?> handelAuthenticationFailedException(
            AuthenticationFailedException ex, WebRequest request) {
        ErrorResponse errorDetails = new ErrorResponse(
                new Date(),
                ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> globleExceptionHandler(
            Exception ex, WebRequest request) {
        ErrorResponse errorDetails = new ErrorResponse(
            new Date(),
            ex.getMessage(),
            request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
