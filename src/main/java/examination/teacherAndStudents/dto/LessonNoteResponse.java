
package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class LessonNoteResponse {
        private Long id;
        private Long lessonPlanId;
        private String topic;
        private String keyPoints;
        private String functions;
        private String title;
        private String content;
        private String diagram;
        private Long studentTermId;
        private String studentTermName;
        private Long teacherId;
        private String teacherName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }