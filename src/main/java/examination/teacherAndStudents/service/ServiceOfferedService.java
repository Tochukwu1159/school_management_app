package examination.teacherAndStudents.service;

import examination.teacherAndStudents.entity.ServiceOffered;

import java.util.List;

public interface ServiceOfferedService {
    ServiceOffered createServiceOffered(ServiceOffered serviceOffered);
    ServiceOffered updateServiceOffered(Long id, ServiceOffered serviceOffered);
    void deleteServiceOffered(Long id);
    List<ServiceOffered> getAllServicesOffered();
    ServiceOffered getServiceOfferedById(Long id);
}
