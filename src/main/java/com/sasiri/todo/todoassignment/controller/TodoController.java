package com.sasiri.todo.todoassignment.controller;

import com.sasiri.todo.todoassignment.entity.Todo;
import com.sasiri.todo.todoassignment.entity.User;
import com.sasiri.todo.todoassignment.repository.TodoRepository;
import com.sasiri.todo.todoassignment.security.UserPrincipal;
import com.sasiri.todo.todoassignment.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/todos")
public class TodoController {
    @Autowired
    private TodoRepository todoRepository;

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
        return todoRepository.save(todo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(@AuthenticationPrincipal UserPrincipal currentUser,
                                        @PathVariable Long id,
                                        @RequestBody Todo todoRequest) {
        return todoRepository.findByIdAndUserId(id, currentUser.getId())
                .map(todo -> {
                    todo.setTitle(todoRequest.getTitle());
                    todo.setDescription(todoRequest.getDescription());
                    todo.setCompleted(todoRequest.isCompleted());
                    todo.setDueDate(todoRequest.getDueDate());
                    todo.setPriority(todoRequest.getPriority());
                    Todo updatedTodo = todoRepository.save(todo);
                    return ResponseEntity.ok(updatedTodo);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@AuthenticationPrincipal UserPrincipal currentUser,
                                        @PathVariable Long id) {
        return todoRepository.findByIdAndUserId(id, currentUser.getId())
                .map(todo -> {
                    todoRepository.delete(todo);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}