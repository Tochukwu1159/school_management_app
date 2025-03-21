package examination.teacherAndStudents.factory;

import examination.teacherAndStudents.templates.idCard.IdCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class IdCardFactory {

    @Autowired
    private Map<String, IdCard> idCardMap; // Spring injects all IdCard implementations

    public IdCard getIdCard(String schoolName) {
        return idCardMap.get(schoolName + "IdCard");
    }
}
