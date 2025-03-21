package examination.teacherAndStudents.templates.idCard;

import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;

public interface IdCard {
    String generateIdCard(User student, Profile profile);
}
