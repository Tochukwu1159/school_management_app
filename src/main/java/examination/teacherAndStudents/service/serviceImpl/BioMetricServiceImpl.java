package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.entity.BiometricTemplate;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.BiometricTemplateRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.service.BiometricService;
import examination.teacherAndStudents.dto.BiometricVerificationResult;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BioMetricServiceImpl implements BiometricService {

    private final BiometricTemplateRepository templateRepository;
    private final ProfileRepository profileRepository;

    @Override
    public BiometricVerificationResult verifyThumbprint(byte[] thumbprintData, Long staffId) {
        // 1. Get stored template for the staff member
        Profile staff = profileRepository.findById(staffId)
                .orElseThrow(() -> new CustomNotFoundException("Staff not found with ID: " + staffId));

        Optional<BiometricTemplate> templateOpt = templateRepository.findByStaff(staff);

        if (templateOpt.isEmpty()) {
            throw new CustomNotFoundException("No thumbprint template found for staff");
        }

        // 2. Perform matching (using external SDK or algorithm)
        double score = matchThumbprint(thumbprintData, templateOpt.get().getTemplateData());

        // 3. Return result
        BiometricVerificationResult result = new BiometricVerificationResult();
        result.setVerified(score >= 0.7); // Example threshold
        result.setScore(score);
        result.setTemplateHash(hashTemplate(thumbprintData));

        return result;
    }

    @Override
    public void enrollThumbprint(byte[] thumbprintData, Long staffId) {
        // Validate input
            Profile staff = profileRepository.findById(staffId)
                .orElseThrow(() -> new CustomNotFoundException("Staff not found with ID: " + staffId));

        if (thumbprintData == null || thumbprintData.length == 0) {
            throw new IllegalArgumentException("Invalid thumbprint data");
        }

        // Create template
        byte[] templateData = createTemplate(thumbprintData);

        // Save or update template
        BiometricTemplate template = templateRepository.findByStaff(staff)
                .orElse(new BiometricTemplate());

        template.setStaff(staff);
        template.setTemplateData(templateData);
        template.setLastUpdated(LocalDateTime.now());

        templateRepository.save(template);
    }

    private double matchThumbprint(byte[] input, byte[] storedTemplate) {
        // Implement actual matching logic here
        // This would use a biometric SDK like Neurotechnology, Innovatrics, etc.
        return 0.8; // Example score
    }

    private byte[] createTemplate(byte[] rawData) {
        // Implement template creation logic
        return rawData; // Simplified example
    }

    private String hashTemplate(byte[] template) {
        // Implement secure hashing
        return DigestUtils.sha256Hex(template);
    }
}
