package examination.teacherAndStudents.templates.reportCard;

import examination.teacherAndStudents.dto.ResultSummary;
import examination.teacherAndStudents.entity.Result;
import org.springframework.stereotype.Component;


@Component("PrinceReportCard")
public class PrinceReportCard implements ReportCard {
    @Override
    public String generateReport(ResultSummary resultSummary) {
        StringBuilder report = new StringBuilder();
        report.append("========================================\n");
        report.append("           PRINCE ACADEMY\n");
        report.append("           Where Leaders are Made\n");
        report.append("========================================\n");
        report.append("Student ID: ").append(resultSummary.getStudentId()).append("\n");
        report.append("Class Level: ").append(resultSummary.getClassLevelId()).append("\n");
        report.append("Session: ").append(resultSummary.getSessionId()).append("\n");
        report.append("Term: ").append(resultSummary.getTerm()).append("\n\n");
        report.append("Academic Performance:\n");
        report.append("+----------------+------------+---------------+------------+--------+--------+\n");
        report.append("| Subject        | Exam Score | Assessment Score | Total Marks | Grade  | Rating |\n");
        report.append("+----------------+------------+---------------+------------+--------+--------+\n");

        resultSummary.getScores().forEach((subject, scores) -> {
            String subjectName = (String) scores.get("subject"); // Get subject name
            int examScore = (int) scores.get("examScore");
            int assessmentScore = (int) scores.get("assessmentScore");

            // Find the corresponding result for this subject
            Result result = resultSummary.getResults().stream()
                    .filter(r -> r.getSubjectName().equals(subject))
                    .findFirst()
                    .orElse(null);

            if (result != null) {
                double totalMarks = result.getTotalMarks();
                String grade = result.getGrade();
                String rating = result.getRating();

                report.append(String.format("| %-14s | %-10d | %-15d | %-10.2f | %-6s | %-6s |\n",
                        subjectName, examScore, assessmentScore, totalMarks, grade, rating));
            }
        });

        report.append("+----------------+------------+---------------+------------+--------+--------+\n");
        report.append("Average Score: ").append(resultSummary.getAverageScore()).append("\n");
        report.append("Position Rank: ").append(resultSummary.getPositionRank()).append("\n\n");
        report.append("Principal's Signature: ________________\n");
        report.append("========================================\n");
        return report.toString();
    }
}
