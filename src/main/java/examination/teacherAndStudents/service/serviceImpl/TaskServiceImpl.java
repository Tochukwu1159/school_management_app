package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.TaskRequest;
import examination.teacherAndStudents.dto.TaskResponse;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.Task;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.TaskRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.TaskService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public TaskResponse saveTask(TaskRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();


        Profile assignedBy = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Profile not found for user: " + email));

        Profile assignedTo = profileRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new CustomNotFoundException("Profile not found for ID: " + request.getAssignedToId()));

        Task task = new Task();
        task.setAssignedBy(assignedBy);
        task.setAssignedTo(assignedTo);
        task.setDescription(request.getDescription());
        task.setFeedback(request.getFeedback());
        task.setStatus(request.getStatus());

        task = taskRepository.save(task);

        return toResponse(task);
    }
    @Override
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setFeedback(request.getFeedback());
        task.setStatus(request.getStatus());

        task = taskRepository.save(task);

        return toResponse(task);
    }

    @Override
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return toResponse(task);
    }

    @Override
    public List<TaskResponse> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Task not found");
        }
        taskRepository.deleteById(id);
    }

    private TaskResponse toResponse(Task task) {
        User assignedBy = userRepository.findById(task.getAssignedBy().getUser().getId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        User assignedTo = userRepository.findById(task.getAssignedTo().getUser().getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return TaskResponse.builder()
                .id(task.getId())
                .assignedById(task.getAssignedBy().getId())
                .assignedByName(assignedBy.getFirstName() + " " +  assignedBy.getLastName())
                .assignedToId(task.getAssignedTo().getId())
                .assignedToName(assignedTo.getFirstName() + " " + assignedTo.getLastName())
                .description(task.getDescription())
                .feedback(task.getFeedback())
                .status(task.getStatus())
                .build();
    }
}
