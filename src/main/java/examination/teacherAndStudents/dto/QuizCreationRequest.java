package examination.teacherAndStudents.dto;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class QuizCreationRequest {
    private Integer numQuestions = 30;
    private Long subjectId;
    private String title;
    private int duration;
    private Integer questionsPerStudent;
    private MultipartFile file;
    private LocalDateTime quizTime;
}
