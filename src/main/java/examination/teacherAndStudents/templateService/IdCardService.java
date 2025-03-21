package examination.teacherAndStudents.templateService;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.factory.IdCardFactory;

import examination.teacherAndStudents.templates.idCard.IdCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class IdCardService {

    @Autowired
    private IdCardFactory idCardFactory;

    public String generateIdCard(String schoolName, User student, Profile profile) {
        IdCard idCard = idCardFactory.getIdCard(schoolName);
        if (idCard == null) {
            throw new IllegalArgumentException("No ID card template found for school: " + schoolName);
        }
        return idCard.generateIdCard(student, profile);
    }
}
