package examination.teacherAndStudents.templates.idCard;

import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import org.springframework.stereotype.Component;

@Component("PrincessIdCard")
public class PrincessIdCard implements IdCard {
    @Override
    public String generateIdCard(User student, Profile profile) {
        // Custom template for Princess Academy ID Card
        StringBuilder idCard = new StringBuilder();
        idCard.append("========================================\n");
        idCard.append("           PRINCESS ACADEMY\n");
        idCard.append("           Empowering Young Minds\n");
        idCard.append("========================================\n");
        idCard.append("Student ID: ").append(student.getId()).append("\n");
        idCard.append("Name: ").append(student.getFirstName() + student.getLastName()).append("\n");
        idCard.append("Class: ").append(profile.getClassBlock()).append("\n");
//        idCard.append("Session: ").append(student.getSession()).append("\n");
        idCard.append("========================================\n");
        idCard.append("School Logo: [Princess Academy Logo]\n");
        idCard.append("========================================\n");
        return idCard.toString();
    }
}