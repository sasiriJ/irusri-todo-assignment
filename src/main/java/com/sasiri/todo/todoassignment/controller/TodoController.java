package com.sasiri.todo.todoassignment.controller;

import com.sasiri.todo.todoassignment.entity.Todo;
import com.sasiri.todo.todoassignment.entity.User;
import com.sasiri.todo.todoassignment.repository.TodoRepository;
import com.sasiri.todo.todoassignment.security.UserPrincipal;
import com.sasiri.todo.todoassignment.service.AuthService;
import com.sasiri.todo.todoassignment.service.TodoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;


@RestController
@RequestMapping("/api/todos")
@Slf4j
public class TodoController {
    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TodoService todoService;

    @Autowired
    private AuthService authService;

    @GetMapping
    public Page<Todo> getTodos(@AuthenticationPrincipal UserPrincipal currentUser,
                               Pageable pageable,
                               @RequestParam(required = false) String search) {
        if (search != null) {
            return todoRepository.findByUserIdAndTitleContaining(
                    currentUser.getId(), search, pageable);
        }
        return todoRepository.findByUserId(currentUser.getId(), pageable);
    }

    @PostMapping
    public Todo createTodo(@AuthenticationPrincipal UserPrincipal currentUser,
                           @RequestBody Todo todo) {
        User currentUser1 = authService.getCurrentUser();
        todo.setUser(currentUser1);
        return todoService.createTodo(todo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(@AuthenticationPrincipal UserPrincipal currentUser,
                                        @PathVariable Long id,
                                        @RequestBody Todo todoRequest) {
        Todo todo = todoService.updateTodo(id, currentUser.getId(), todoRequest);

        if (todo == null) {
            return ResponseEntity.notFound().build();
        }else {
            return ResponseEntity.ok(todo);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@AuthenticationPrincipal UserPrincipal currentUser,
                                        @PathVariable Long id) {
        todoService.deleteTodo(id,currentUser.getId());
        return ResponseEntity.ok().build();
    }
    @GetMapping("/search")
    public ResponseEntity<Page<Todo>> searchTodos(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false, defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String direction) {

        log.debug("Search request received - search: {}, completed: {}, priority: {}, sortBy: {}, page: {}, size: {}",
                search, completed, priority, sortBy, page, size);

        List<String> allowedSortFields = Arrays.asList("title", "createdAt", "priority", "dueDate");
        if (!allowedSortFields.contains(sortBy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Sort field must be one of: " + String.join(", ", allowedSortFields));
        }

        // Create Pageable object with sort direction
        Sort sort = direction.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        try {
            Page<Todo> todos = todoService.getTodosWithFilters(
                    currentUser.getId(),
                    search,
                    completed,
                    priority,
                    sortBy,
                    pageable
            );

            log.debug("Found {} todos matching the criteria", todos.getTotalElements());

            return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(todos.getTotalElements()))
                    .body(todos);

        } catch (Exception e) {
            log.error("Error while searching todos", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error while searching todos"
            );
        }
    }
}