package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ScoreRequest;
import examination.teacherAndStudents.entity.Score;
import examination.teacherAndStudents.service.serviceImpl.ScoreServiceImpl;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ScoreService {
    void addScore(ScoreRequest scoreRequest);
    List<Score> getScoresByStudent(Long studentId);
   void addScoresFromCsv(MultipartFile file);
}