package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class LessonPlannerRequest {
    private Long teacherId;
    private Long schoolId;
    private Long termId;
    private Long classBlockId;

    private String week;
    private String day;

    private String status;  // APPROVED, PENDING, REJECTED
    private String teachingStatus;  // PLANNED, ONGOING, COMPLETED, CANCELLED, MISSED, PENDING
    private String classAssessment;
    private String homeAssessment;
    private String priorKnowledge;

    private String lessonTopic;
    private String lessonObjectives;  // Goals or outcomes of the lesson
    private String resources;         // Materials or references for the lesson

    private String period;            // E.g., "9:00am - 10:00am"
    private int durationInMinutes;    // Duration of the lesson in minutes

    private String notes;             // Additional notes or comments
    private String teachingMethod;    // E.g., lecture, group discussion, etc.

    private String feedback;          // Post-lesson feedback or evaluation
    private String updatedTimeAfterTeaching;

    private int version;              // Version of the lesson plan

    private Long subjectId;
}
