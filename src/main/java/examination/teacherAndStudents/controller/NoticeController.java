package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.NoticeRequest;
import examination.teacherAndStudents.dto.NoticeResponse;
import examination.teacherAndStudents.dto.UpdateNoticeRequest;
import examination.teacherAndStudents.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notice")
public class NoticeController {

    private final NoticeService noticeService;

    @Autowired
    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<NoticeResponse>>> getAllNoticePosts() {
        try {
            List<NoticeResponse> blogPosts = noticeService.getAllNoticePosts();
            return ResponseEntity.ok(new ApiResponse<>("Posts fetched successfully", true, blogPosts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("An error occurred", false));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeResponse>> getNoticePostById(@PathVariable Long id) {
        try {
            NoticeResponse blogPost = noticeService.getNoticePostById(id);
            if (blogPost != null) {
                return ResponseEntity.ok(new ApiResponse<>("Post found", true, blogPost));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>("Post not found", false));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("An error occurred", false));
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<NoticeResponse>>> getEventsByDateRange(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<NoticeResponse> events = noticeService.getEventsByDateRange(start, end);
            return ResponseEntity.ok(new ApiResponse<>("Events fetched successfully", true, events));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("An error occurred", false));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<NoticeResponse>> createNoticePost(@RequestBody NoticeRequest blogPost) {
        try {
            NoticeResponse createdNoticePost = noticeService.createNoticePost(blogPost);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Post created successfully", true, createdNoticePost));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("An error occurred", false));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeResponse>> updateNoticePost(@PathVariable Long id,
                                                                        @RequestBody UpdateNoticeRequest updatedNoticePost) {
        try {
            NoticeResponse updatedPost = noticeService.updateNoticePost(id, updatedNoticePost);
            if (updatedPost != null) {
                return ResponseEntity.ok(new ApiResponse<>("Post updated successfully", true, updatedPost));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>("Post not found", false));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("An error occurred", false));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNoticePost(@PathVariable Long id) {
        try {
            boolean deleted = noticeService.deleteNoticePost(id);
            if (deleted) {
                return ResponseEntity.ok(new ApiResponse<>("Post deleted successfully", true, null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>("Post not found", false));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("An error occurred", false));
        }
    }
}
