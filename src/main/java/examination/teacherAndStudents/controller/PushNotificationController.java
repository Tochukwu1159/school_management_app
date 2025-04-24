//package examination.teacherAndStudents.controller;
//import examination.teacherAndStudents.dto.DeviceRegistrationDto;
//import examination.teacherAndStudents.dto.PushNotificationDto;
//import examination.teacherAndStudents.service.PushNotificationService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//@RestController
//@RequestMapping("/api/notifications")
//@RequiredArgsConstructor
//public class PushNotificationController {
//
//    private final PushNotificationService pushService;
//
//    @PostMapping("/register")
//    public ResponseEntity<Void> registerDevice(
//            @Valid @RequestBody DeviceRegistrationDto dto) {
//        pushService.registerDevice(dto.userId(), dto.fcmToken(), dto.deviceId());
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/send")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Void> sendNotification(
//            @Valid @RequestBody PushNotificationDto dto) {
//        if (dto.templateCode() != null) {
//            pushService.sendTemplatedNotification(
//                    dto.userId(),
//                    dto.templateCode(),
//                    dto.variables());
//        } else {
//            pushService.sendPushNotification(
//                    dto.userId(),
//                    dto.title(),
//                    dto.content());
//        }
//        return ResponseEntity.ok().build();
//    }
//}