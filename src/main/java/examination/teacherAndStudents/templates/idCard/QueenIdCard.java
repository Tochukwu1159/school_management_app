package examination.teacherAndStudents.templates.idCard;

import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import org.springframework.stereotype.Component;

@Component("QueenIdCard")
public class QueenIdCard implements IdCard {
    @Override
    public String generateIdCard(User student, Profile profile) {
        // Custom template for Queen Academy ID Card
        StringBuilder idCard = new StringBuilder();
        idCard.append("========================================\n");
        idCard.append("           QUEEN ACADEMY\n");
        idCard.append("           Excellence in Education\n");
        idCard.append("========================================\n");
        idCard.append("Student ID: ").append(student.getId()).append("\n");
        idCard.append("Name: ").append(student.getFirstName()).append(" ").append(student.getLastName()).append("\n");
        idCard.append("Class: ").append(profile.getClassBlock()).append("\n");
//        idCard.append("Session: ").append(student.getSession()).append("\n");
        idCard.append("========================================\n");
        idCard.append("School Logo: [Queen Academy Logo]\n");
        idCard.append("========================================\n");
        return idCard.toString();
    }
}