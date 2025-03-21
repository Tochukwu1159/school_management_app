package examination.teacherAndStudents.templates.idCard;

import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import org.springframework.stereotype.Component;

@Component("PrinceIdCard")
public class PrinceIdCard implements IdCard {
    @Override
    public String generateIdCard(User student, Profile profile) {
        System.out.println(profile);
        System.out.println(profile);
        // Custom template for Prince Academy ID Card
        StringBuilder idCard = new StringBuilder();
        idCard.append("========================================\n");
        idCard.append("           PRINCE ACADEMY\n");
        idCard.append("           Where Leaders are Made\n");
        idCard.append("========================================\n");
        idCard.append("Student ID: ").append(student.getId()).append("\n");
        idCard.append("Name: ").append(student.getFirstName()).append(" ").append(student.getLastName()).append("\n");
        idCard.append("Class: ").append(profile.getClassBlock().getCurrentStudentClassName()).append("\n");
//        idCard.append("Session: ").append(student.getSession()).append("\n");
        idCard.append("========================================\n");
        idCard.append("School Logo: [Prince Academy Logo]\n");
        idCard.append("========================================\n");
        return idCard.toString();
    }
}
