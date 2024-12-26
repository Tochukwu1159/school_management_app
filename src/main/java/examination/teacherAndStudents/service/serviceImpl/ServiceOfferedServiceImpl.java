package examination.teacherAndStudents.service.impl;

import examination.teacherAndStudents.entity.ServiceOffered;
import examination.teacherAndStudents.repository.ServiceOfferedRepository;
import examination.teacherAndStudents.service.ServiceOfferedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceOfferedServiceImpl implements ServiceOfferedService {

    @Autowired
    private ServiceOfferedRepository serviceOfferedRepository;

    @Override
    public ServiceOffered createServiceOffered(ServiceOffered serviceOffered) {
        return serviceOfferedRepository.save(serviceOffered);
    }

    @Override
    public ServiceOffered updateServiceOffered(Long id, ServiceOffered serviceOffered) {
        ServiceOffered existingService = serviceOfferedRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with ID: " + id));
        existingService.setName(serviceOffered.getName());
        return serviceOfferedRepository.save(existingService);
    }

    @Override
    public void deleteServiceOffered(Long id) {
        serviceOfferedRepository.deleteById(id);
    }

    @Override
    public List<ServiceOffered> getAllServicesOffered() {
        return serviceOfferedRepository.findAll();
    }

    @Override
    public ServiceOffered getServiceOfferedById(Long id) {
        return serviceOfferedRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with ID: " + id));
    }
}
