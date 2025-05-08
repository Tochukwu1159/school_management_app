package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.ClassBlockRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.service.PromotionService;
import examination.teacherAndStudents.dto.StudentPromotionRequest;
import org.springframework.stereotype.Service;

@Service
public class PromotionServiceImpl implements PromotionService {
    private final ClassBlockRepository classBlockRepository;
    private final ProfileRepository profileRepository;

    public PromotionServiceImpl(ClassBlockRepository classBlockRepository, ProfileRepository profileRepository) {
        this.classBlockRepository = classBlockRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    public void promoteStudents(StudentPromotionRequest request) {
        try {
            int promotedCount = 0;

            for (StudentPromotionRequest.PromotionData data : request.getPromotion()) {

                Profile studentProfile = profileRepository.findById(data.getStudentId())
                        .orElseThrow(() -> new NotFoundException("Student not found with ID: " + data.getStudentId()));

                ClassBlock promotedClassBlock = classBlockRepository.findById(data.getPromotedClassBlock())
                        .orElseThrow(() -> new NotFoundException("Promoted class block not found with ID: " + data.getPromotedClassBlock()));

                // Update student profile
                studentProfile.setClassBlock(promotedClassBlock);
                profileRepository.save(studentProfile);

                // Update promoted class block's student count
                promotedClassBlock.setNumberOfStudents(promotedClassBlock.getNumberOfStudents() + 1);
                classBlockRepository.save(promotedClassBlock);

                promotedCount++;
            }

            // Optional: Log or return the count of promoted students
            System.out.println("Total promoted students: " + promotedCount);

        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred during student promotion: " + e.getMessage(), e);

        }
    }
}