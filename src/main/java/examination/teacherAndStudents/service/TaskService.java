package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.TaskRequest;
import examination.teacherAndStudents.dto.TaskResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TaskService {
    TaskResponse saveTask(TaskRequest request);    // Add new task
    TaskResponse updateTask(Long id, TaskRequest request);  // Edit task
    TaskResponse getTaskById(Long id);  // Get task by ID
    Page<TaskResponse> getAllTasks(int page, int size, String sortBy, String sortDirection);  // Get all tasks
    void deleteTask(Long id);  // Delete task
}
