package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.NoticeRequest;
import examination.teacherAndStudents.dto.NoticeResponse;
import examination.teacherAndStudents.dto.UpdateNoticeRequest;

import java.time.LocalDate;
import java.util.List;

public interface NoticeService {
    List<NoticeResponse> getAllNoticePosts();
    NoticeResponse getNoticePostById(Long id);
    List<NoticeResponse> getEventsByDateRange(LocalDate startDate, LocalDate endDate);
    NoticeResponse createNoticePost(NoticeRequest blogPost);
    NoticeResponse updateNoticePost(Long id, UpdateNoticeRequest updatedNoticePost) ;
    boolean deleteNoticePost(Long id);
}
