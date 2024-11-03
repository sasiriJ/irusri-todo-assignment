package com.sasiri.todo.todoassignment;

import com.sasiri.todo.todoassignment.entity.Todo;
import com.sasiri.todo.todoassignment.entity.User;
import com.sasiri.todo.todoassignment.repository.TodoRepository;
import com.sasiri.todo.todoassignment.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TodoAssignmentApplicationTests {
    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Test
    void contextLoads() {
    }
    @Test
    public void testCreateUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password"));

        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getId());
        assertTrue(passwordEncoder.matches("password", savedUser.getPassword()));
        userRepository.delete(user);
    }
    @Test
    public void testCreateTodo() {
        User user = userRepository.save(createTestUser());

        Todo todo = new Todo();
        todo.setTitle("Test Todo");
        todo.setDescription("Test Description");
        todo.setUser(user);
        todo.setDueDate(LocalDateTime.now().plusDays(1));

        Todo savedTodo = todoRepository.save(todo);
        assertNotNull(savedTodo.getId());
        assertEquals("Test Todo", savedTodo.getTitle());
    }

    @Test
    public void testUpdateTodo() {
        User user = userRepository.save(createTestUser());
        Todo todo = createTestTodo(user);

        todo.setTitle("Updated Title");
        todo.setCompleted(true);

        Todo updatedTodo = todoRepository.save(todo);
        assertEquals("Updated Title", updatedTodo.getTitle());
        assertTrue(updatedTodo.isCompleted());
    }

    @Test
    public void testDeleteTodo() {
        User user = userRepository.save(createTestUser());
        Todo todo = createTestTodo(user);

        todoRepository.delete(todo);
        assertFalse(todoRepository.existsById(todo.getId()));
    }

    @Test
    public void testFindTodosByUser() {
        User user = userRepository.save(createTestUser());
        createTestTodo(user);
        createTestTodo(user);

        assertEquals(2, todoRepository.findByUserId(user.getId(), Pageable.unpaged()).getTotalElements());
    }

    private User createTestUser() {
        User user = new User();
        user.setEmail("test" + System.currentTimeMillis() + "@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        return user;
    }

    private Todo createTestTodo(User user) {
        Todo todo = new Todo();
        todo.setTitle("Test Todo");
        todo.setDescription("Test Description");
        todo.setUser(user);
        todo.setDueDate(LocalDateTime.now().plusDays(1));
        return todoRepository.save(todo);
    }
}
