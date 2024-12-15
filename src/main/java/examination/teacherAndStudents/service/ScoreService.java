package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ScoreRequest;
import examination.teacherAndStudents.entity.Score;

import java.util.List;

public interface ScoreService {
    void addScore(ScoreRequest scoreRequest);
    List<Score> getScoresByStudent(Long studentId);
}