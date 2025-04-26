package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.LessonStatus;
import examination.teacherAndStudents.utils.TeachingStatus;
import lombok.*;

import jakarta.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lessoonPLanner")
@Entity
@Builder
public class LessonPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Profile teacher;

    @ManyToOne
    @JoinColumn(name = "approver_id", nullable = false)
    private Profile approvedBy;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private ClassSubject subject;

    @ManyToOne
    @JoinColumn(name = "term_id", nullable = false)
    private StudentTerm term;

    @ManyToOne
    @JoinColumn(name = "class_block_id", nullable = false)
    private ClassBlock classBlock;


    private String lessonObjectives;  // Goals or outcomes of the lesson
    private String resources;         // Materials or references for the lesson
    private String notes;             // Additional notes or comments
    private String lessonOutcome;
    private String priorKnowledge;
    private String settingInduction; // arouse intetesrt
    private String activities;
    private String period;            // E.g., "9:00am - 10:00am"
    private String teachingMethod;    // E.g., lecture, group discussion, etc.
    private String feedback;          // Post-lesson feedback or evaluation
    private String week; //week 1
    private String day;

    private String classAssessment;
    private String homeAssessment;
    private LessonStatus status;  // Approved, Pending, Rejected
    private TeachingStatus teachingStatus;  // PLANNED, ONGOING, COMPLETED, CANCELLED, MISSED, PENDING
    private String lessonTopic;
    private String updatedTimeAfterTeaching;
}

//period not day;