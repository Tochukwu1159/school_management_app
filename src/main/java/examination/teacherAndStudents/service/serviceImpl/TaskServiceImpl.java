package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.TaskRequest;
import examination.teacherAndStudents.dto.TaskResponse;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.Task;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.TaskRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.TaskService;
import examination.teacherAndStudents.utils.Roles;
import examination.teacherAndStudents.utils.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    private User validateAuthenticatedUser(boolean requireAssigner, Long taskId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        if (email == null) {
            throw new CustomNotFoundException("No authenticated user found");
        }

        Set<Roles> allowedRoles = requireAssigner ? Set.of(Roles.ADMIN, Roles.TEACHER) :
                Set.of(Roles.ADMIN, Roles.TEACHER, Roles.STUDENT);
        User user = userRepository.findByEmailAndRolesIn(email, allowedRoles)
                .orElseThrow(() -> new CustomNotFoundException(
                        "Please login as an " + (requireAssigner ? "Admin or Teacher" : "Admin, Teacher, or Student")));
        School school = user.getSchool();
        if (school == null) {
            throw new CustomInternalServerException("User is not associated with any school");
        }
        return user;
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "createdAt";
        }
        // Validate sortBy
        if (!List.of("id", "createdAt", "status", "description").contains(sortBy)) {
            log.warn("Unknown sort field '{}', defaulting to 'createdAt'", sortBy);
            sortBy = "createdAt";
        }
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sort direction: " + sortDirection + ". Use 'ASC' or 'DESC'");
        }
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    @Transactional
    @Override
    public TaskResponse saveTask(TaskRequest request) {
        try {
            User user = validateAuthenticatedUser(true, null);
            School school = user.getSchool();

            // Validate request
            if (request.getAssignedToId() == null) {
                throw new IllegalArgumentException("Assigned to ID must not be null");
            }
            if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("Task description must not be null or empty");
            }
            if (request.getStatus() != null && !TaskStatus.isValid(request.getStatus())) {
                throw new IllegalArgumentException("Invalid status: " + request.getStatus());
            }

            Profile assignedBy = profileRepository.findByUserEmail(user.getEmail())
                    .orElseThrow(() -> new CustomNotFoundException("Profile not found for user: " + user.getEmail()));
            Profile assignedTo = profileRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new CustomNotFoundException("Profile not found with ID: " + request.getAssignedToId()));

            if (!school.equals(assignedTo.getUser().getSchool())) {
                throw new CustomNotFoundException("Assigned to profile does not belong to your school");
            }

            Task task = Task.builder()
                    .assignedBy(assignedBy)
                    .assignedTo(assignedTo)
                    .school(assignedBy.getUser().getSchool())
                    .description(request.getDescription().trim())
                    .status(request.getStatus() != null ? request.getStatus() : TaskStatus.PENDING.getValue())
                    .build();

            Task savedTask = taskRepository.save(task);
            log.info("Created task ID {} by user {} for school ID {}", savedTask.getId(), user.getEmail(), school.getId());
            return toResponse(savedTask);
        } catch (IllegalArgumentException | CustomNotFoundException e) {
            log.error("Error saving task: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error saving task: {}", e.getMessage(), e);
            throw new CustomInternalServerException("Failed to save task: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public TaskResponse updateTask(Long id, TaskRequest request) {
        try {
            User user = validateAuthenticatedUser(false, id);
            School school = user.getSchool();

            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new CustomNotFoundException("Task not found with ID: " + id));

            Profile currentProfile = profileRepository.findByUserEmail(user.getEmail())
                    .orElseThrow(() -> new CustomNotFoundException("Profile not found for user: " + user.getEmail()));
            boolean isAdminOrTeacher = user.getRoles().contains(Roles.ADMIN) || user.getRoles().contains(Roles.TEACHER);
            boolean isAssignee = task.getAssignedTo().getId().equals(currentProfile.getId());

            if (!isAdminOrTeacher && !isAssignee) {
                throw new CustomNotFoundException("You are not authorized to update task ID: " + id);
            }
            if (!school.equals(task.getAssignedBy().getUser().getSchool())) {
                throw new CustomNotFoundException("Task ID " + id + " does not belong to your school");
            }

            // Validate and update fields
            if (request.getAssignedToId() != null && isAdminOrTeacher) {
                Profile assignedTo = profileRepository.findById(request.getAssignedToId())
                        .orElseThrow(() -> new CustomNotFoundException("Profile not found with ID: " + request.getAssignedToId()));
                if (!school.equals(assignedTo.getUser().getSchool())) {
                    throw new CustomNotFoundException("Assigned to profile does not belong to your school");
                }
                task.setAssignedTo(assignedTo);
            }
            if (request.getDescription() != null && !request.getDescription().trim().isEmpty() && isAdminOrTeacher) {
                task.setDescription(request.getDescription().trim());
            }
            if (request.getFeedback() != null && isAssignee) {
                task.setFeedback(request.getFeedback().trim());
            }
            if (request.getStatus() != null) {
                if (!TaskStatus.isValid(request.getStatus())) {
                    throw new IllegalArgumentException("Invalid status: " + request.getStatus());
                }
                task.setStatus(request.getStatus());
            }

            Task updatedTask = taskRepository.save(task);
            log.info("Updated task ID {} by user {} in school ID {}", id, user.getEmail(), school.getId());
            return toResponse(updatedTask);
        } catch (IllegalArgumentException | CustomNotFoundException e) {
            log.error("Error updating task ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating task ID {}: {}", id, e.getMessage(), e);
            throw new CustomInternalServerException("Failed to update task ID " + id + ": " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public TaskResponse getTaskById(Long id) {
        try {
            User user = validateAuthenticatedUser(false, id);
            School school = user.getSchool();

            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new CustomNotFoundException("Task not found with ID: " + id));

            Profile currentProfile = profileRepository.findByUserEmail(user.getEmail())
                    .orElseThrow(() -> new CustomNotFoundException("Profile not found for user: " + user.getEmail()));
            boolean isAdminOrTeacher = user.getRoles().contains(Roles.ADMIN) || user.getRoles().contains(Roles.TEACHER);
            boolean isAssignerOrAssignee = task.getAssignedBy().getId().equals(currentProfile.getId()) ||
                    task.getAssignedTo().getId().equals(currentProfile.getId());

            if (!isAdminOrTeacher && !isAssignerOrAssignee) {
                throw new CustomNotFoundException("You are not authorized to view task ID: " + id);
            }
            if (!school.equals(task.getAssignedBy().getUser().getSchool())) {
                throw new CustomNotFoundException("Task ID " + id + " does not belong to your school");
            }

            log.info("Fetched task ID {} by user {} in school ID {}", id, user.getEmail(), school.getId());
            return toResponse(task);
        } catch (CustomNotFoundException e) {
            log.error("Error fetching task ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching task ID {}: {}", id, e.getMessage(), e);
            throw new CustomInternalServerException("Failed to fetch task ID " + id + ": " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TaskResponse> getAllTasks(int page, int size, String sortBy, String sortDirection) {
        try {
            User user = validateAuthenticatedUser(false, null);
            School school = user.getSchool();

            Pageable pageable = createPageable(page, size, sortBy, sortDirection);
            Page<Task> tasks;
            boolean isAdminOrTeacher = user.getRoles().contains(Roles.ADMIN) || user.getRoles().contains(Roles.TEACHER);

            if (isAdminOrTeacher) {
                tasks = taskRepository.findAllBySchoolId(school.getId(), pageable);
            } else {
                Profile profile = profileRepository.findByUserEmail(user.getEmail())
                        .orElseThrow(() -> new CustomNotFoundException("Profile not found for user: " + user.getEmail()));
                tasks = taskRepository.findAllByAssignedByOrAssignedToAndSchoolId(
                        profile, profile, school.getId(), pageable);
            }

            log.info("Fetched {} tasks for user {} in school ID {}", tasks.getTotalElements(), user.getEmail(), school.getId());
            return tasks.map(this::toResponse);
        } catch (IllegalArgumentException | CustomNotFoundException e) {
            log.error("Error fetching tasks: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching tasks: {}", e.getMessage(), e);
            throw new CustomInternalServerException("Failed to fetch tasks: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void deleteTask(Long id) {
        try {
            User user = validateAuthenticatedUser(true, id);
            School school = user.getSchool();

            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new CustomNotFoundException("Task not found with ID: " + id));

            if (!school.equals(task.getAssignedBy().getUser().getSchool())) {
                throw new CustomNotFoundException("Task ID " + id + " does not belong to your school");
            }
            if (!TaskStatus.PENDING.getValue().equalsIgnoreCase(task.getStatus())) {
                throw new IllegalStateException("Cannot delete task ID " + id + " with status: " + task.getStatus());
            }

            taskRepository.delete(task);
            log.info("Deleted task ID {} by user {} in school ID {}", id, user.getEmail(), school.getId());
        } catch (CustomNotFoundException | IllegalStateException e) {
            log.error("Error deleting task ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting task ID {}: {}", id, e.getMessage(), e);
            throw new CustomInternalServerException("Failed to delete task ID " + id + ": " + e.getMessage());
        }
    }

    private TaskResponse toResponse(Task task) {
        String assignedByName = task.getAssignedBy().getUser().getFirstName() + " " +
                task.getAssignedBy().getUser().getLastName();
        String assignedToName = task.getAssignedTo().getUser().getFirstName() + " " +
                task.getAssignedTo().getUser().getLastName();

        return TaskResponse.builder()
                .id(task.getId())
                .assignedById(task.getAssignedBy().getId())
                .assignedByName(assignedByName.trim())
                .assignedToId(task.getAssignedTo().getId())
                .assignedToName(assignedToName.trim())
                .description(task.getDescription())
                .dateAssigned(task.getCreatedAt())
                .feedback(task.getFeedback())
                .status(task.getStatus())
                .build();
    }
}