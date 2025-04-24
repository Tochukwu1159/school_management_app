package examination.teacherAndStudents.service;

import examination.teacherAndStudents.entity.ServiceOffered;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ServiceOfferedService {
    ServiceOffered createServiceOffered(String name, boolean isDefault);
    ServiceOffered updateServiceOffered(Long id, String name, boolean isDefault);
    void deleteServiceOffered(Long id);
    ServiceOffered getServiceOfferedById(Long id);

    Page<ServiceOffered> getAllServicesOffered(String name, Boolean isDefault, Pageable pageable);
}
