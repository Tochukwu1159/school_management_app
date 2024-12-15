package examination.teacherAndStudents.controller;
import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.NotificationResponse;
import examination.teacherAndStudents.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/notification")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/student")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> retrieveStudentNotification() {
        return new ResponseEntity<>(new ApiResponse<>("Success",true,notificationService.allNotificationsOfA_StudentById()), HttpStatus.OK);
    }



}
