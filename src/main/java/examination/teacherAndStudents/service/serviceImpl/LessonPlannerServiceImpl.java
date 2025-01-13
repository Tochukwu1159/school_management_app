package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.LessonPlannerRequest;
import examination.teacherAndStudents.dto.LessonPlannerResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.LessonPlannerService;
import examination.teacherAndStudents.utils.EntityFetcher;
import examination.teacherAndStudents.utils.LessonStatus;
import examination.teacherAndStudents.utils.TeachingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonPlannerServiceImpl implements LessonPlannerService {

    private final LessonPlannerRepository lessonPlannerRepository;
    private final EntityFetcher entityFetcher;

    @Override
    public LessonPlannerResponse createLessonPlanner(LessonPlannerRequest request) {



        ClassSubject subject = entityFetcher.fetchClassSubject(request.getSubjectId());
        String email = entityFetcher.fetchLoggedInUser();
        ClassBlock classBlock = entityFetcher.fetchClassBlock(request.getClassBlockId());
        User teacher = entityFetcher.fetchUser(request.getTeacherId());
        Profile teacherProfile = entityFetcher.fetchProfileByUser(teacher);
        User admin = entityFetcher.fetchLoggedInAdmin(email);
        StudentTerm term = entityFetcher.fetchStudentTerm(request.getTermId());
        School school = admin.getSchool();

        LessonPlan lessonPlan = new LessonPlan();
        lessonPlan.setTeacher(teacherProfile);
        lessonPlan.setSchool(school);
        lessonPlan.setTerm(term);
        lessonPlan.setClassBlock(classBlock);
        lessonPlan.setWeek(request.getWeek());
        lessonPlan.setNotes(request.getNotes());
        lessonPlan.setDay(request.getDay());
        lessonPlan.setHomeAssessment(request.getHomeAssessment());
        lessonPlan.setLessonObjectives(request.getLessonObjectives());
        lessonPlan.setClassAssessment(request.getClassAssessment());
        lessonPlan.setPriorKnowledge(request.getPriorKnowledge());
        lessonPlan.setResources(request.getResources());
        lessonPlan.setTeachingMethod(request.getTeachingMethod());
        lessonPlan.setSubject(subject);
        lessonPlan.setStatus(LessonStatus.PENDING);
        lessonPlan.setTeachingStatus(TeachingStatus.PENDING);

        lessonPlan.setLessonTopic(request.getLessonTopic());
        lessonPlan.setPeriod(request.getPeriod());

        lessonPlan = lessonPlannerRepository.save(lessonPlan);

        return mapToResponse(lessonPlan);
    }

    @Override
    public LessonPlannerResponse updateLessonPlanner(Long id, LessonPlannerRequest request) {
        LessonPlan lessonPlan = lessonPlannerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lesson planner not found"));

        lessonPlan.setWeek(request.getWeek());
        lessonPlan.setDay(request.getDay());
        lessonPlan.setStatus(LessonStatus.valueOf(request.getStatus().toUpperCase()));
        lessonPlan.setTeachingStatus(TeachingStatus.valueOf(request.getTeachingStatus().toUpperCase()));

        lessonPlan.setLessonTopic(request.getLessonTopic());
        lessonPlan.setPeriod(request.getPeriod());
        lessonPlan.setUpdatedTimeAfterTeaching(request.getUpdatedTimeAfterTeaching());

        lessonPlan = lessonPlannerRepository.save(lessonPlan);
        return mapToResponse(lessonPlan);
    }

    @Override
    public void deleteLessonPlanner(Long id) {
        lessonPlannerRepository.deleteById(id);
    }

    @Override
    public LessonPlannerResponse getLessonPlannerById(Long id) {
        LessonPlan lessonPlan = lessonPlannerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lesson planner not found"));
        return mapToResponse(lessonPlan);
    }

    @Override
    public List<LessonPlannerResponse> getAllLessonPlanners() {
        List<LessonPlan> lessonPlans = lessonPlannerRepository.findAll();
        return lessonPlans.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private LessonPlannerResponse mapToResponse(LessonPlan lessonPlan) {
        LessonPlannerResponse response = new LessonPlannerResponse();
        response.setId(lessonPlan.getId());
        response.setWeek(lessonPlan.getWeek());
        response.setDay(lessonPlan.getDay());
        response.setStatus(lessonPlan.getStatus());
        response.setTeachingStatus(lessonPlan.getTeachingStatus());
        response.setLessonTopic(lessonPlan.getLessonTopic());
        response.setPeriod(lessonPlan.getPeriod());
        response.setUpdatedTimeAfterTeaching(lessonPlan.getUpdatedTimeAfterTeaching());
        response.setSchoolName(lessonPlan.getSchool().getSchoolName());
        response.setTermName(lessonPlan.getTerm().getName());
        response.setClassBlockName(lessonPlan.getClassBlock().getCurrentStudentClassName());
        return response;
    }
}

