package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.TaskRequest;
import examination.teacherAndStudents.dto.TaskResponse;
import examination.teacherAndStudents.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@RequestBody TaskRequest request) {
        TaskResponse response = taskService.saveTask(request);
        ApiResponse<TaskResponse> apiResponse = new ApiResponse<>("Task created successfully", true, response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(@PathVariable Long id, @RequestBody TaskRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        ApiResponse<TaskResponse> apiResponse = new ApiResponse<>("Task updated successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(@PathVariable Long id) {
        TaskResponse response = taskService.getTaskById(id);
        ApiResponse<TaskResponse> apiResponse = new ApiResponse<>("Task fetched successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Page<TaskResponse> taskPage = taskService.getAllTasks(page, size, sortBy, sortDirection);
        ApiResponse<Page<TaskResponse>> apiResponse = new ApiResponse<>("Tasks fetched successfully", true, taskPage);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>("Task deleted successfully", true, null);
        return new ResponseEntity<>(apiResponse, HttpStatus.NO_CONTENT);
    }
}
