package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.TaskRequest;
import examination.teacherAndStudents.dto.TaskResponse;

import java.util.List;

public interface TaskService {
    TaskResponse saveTask(TaskRequest request);    // Add new task
    TaskResponse updateTask(Long id, TaskRequest request);  // Edit task
    TaskResponse getTaskById(Long id);  // Get task by ID
    List<TaskResponse> getAllTasks();  // Get all tasks
    void deleteTask(Long id);  // Delete task
}
