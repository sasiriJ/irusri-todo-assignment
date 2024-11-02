package com.sasiri.todo.todoassignment.service;

import com.sasiri.todo.todoassignment.entity.Todo;
import com.sasiri.todo.todoassignment.exception.ResourceNotFoundException;
import com.sasiri.todo.todoassignment.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TodoService {
    private static final Logger logger = LoggerFactory.getLogger(TodoService.class);
    
    @Autowired
    private TodoRepository todoRepository;

    public Page<Todo> getTodosWithFilters(
            Long userId,
            String searchTerm,
            Boolean completed,
            Integer priority,
            String sortBy,
            Pageable pageable) {
        logger.info("Fetching todos for user {} with filters: search={}, completed={}, priority={}, sortBy={}",
                   userId, searchTerm, completed, priority, sortBy);
        
        return todoRepository.findWithFilters(userId, searchTerm, completed, priority, sortBy, pageable);
    }

    public Todo createTodo(Todo todo) {
        logger.info("Creating new todo for user {}", todo.getUser().getId());
        return todoRepository.save(todo);
    }

    public Todo updateTodo(Long todoId, Long userId, Todo todoRequest) {
        logger.info("Updating todo {} for user {}", todoId, userId);
        
        return todoRepository.findByIdAndUserId(todoId, userId)
            .map(todo -> {
                todo.setTitle(todoRequest.getTitle());
                todo.setDescription(todoRequest.getDescription());
                todo.setCompleted(todoRequest.isCompleted());
                todo.setDueDate(todoRequest.getDueDate());
                todo.setPriority(todoRequest.getPriority());
                return todoRepository.save(todo);
            })
            .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id " + todoId));
    }

    public void deleteTodo(Long todoId, Long userId) {
        logger.info("Deleting todo {} for user {}", todoId, userId);
        
        Todo todo = todoRepository.findByIdAndUserId(todoId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id " + todoId));
        
        todoRepository.delete(todo);
    }

    public long getCompletedTodosCount(Long userId) {
        return todoRepository.countCompletedByUser(userId);
    }
}