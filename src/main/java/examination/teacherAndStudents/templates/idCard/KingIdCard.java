package examination.teacherAndStudents.templates.idCard;

import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import org.springframework.stereotype.Component;

@Component("KingIdCard")
public class KingIdCard implements IdCard {
    @Override
    public String generateIdCard(User student, Profile profile) {
        // Custom template for King Academy ID Card
        StringBuilder idCard = new StringBuilder();
        idCard.append("========================================\n");
        idCard.append("           KING ACADEMY\n");
        idCard.append("           Building Future Leaders\n");
        idCard.append("========================================\n");
        idCard.append("Student ID: ").append(student.getId()).append("\n");
        idCard.append("Name: ").append(student.getFirstName() + " " +student.getLastName()).append("\n");
        idCard.append("Class: ").append(profile.getSessionClass().getClassBlock()).append("\n");
//        idCard.append("Session: ").append(profile.get).append("\n");
        idCard.append("========================================\n");
        idCard.append("School Logo: [King Academy Logo]\n");
        idCard.append("========================================\n");
        return idCard.toString();
    }
}
