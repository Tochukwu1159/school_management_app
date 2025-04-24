package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.entity.ServiceOffered;
import examination.teacherAndStudents.error_handler.BadRequestException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.ServiceOfferedRepository;
import examination.teacherAndStudents.service.ServiceOfferedService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of ServiceOfferedService for managing services offered.
 */
@Service
@RequiredArgsConstructor
public class ServiceOfferedServiceImpl implements ServiceOfferedService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceOfferedServiceImpl.class);
    private final ServiceOfferedRepository serviceOfferedRepository;

    @Override
    @Transactional
    public ServiceOffered createServiceOffered(String name, boolean isDefault) {
        validateServiceName(name);

        if (serviceOfferedRepository.existsByName(name)) {
            throw new BadRequestException("Service with name '" + name + "' already exists.");
        }

        ServiceOffered service = ServiceOffered.builder()
                .name(name)
                .isDefault(isDefault)
                .build();

        ServiceOffered savedService = serviceOfferedRepository.save(service);
        logger.info("Created service offered ID {} with name {} (default: {})",
                savedService.getId(), name, isDefault);
        return savedService;
    }

    @Override
    @Transactional
    public ServiceOffered updateServiceOffered(Long id, String name, boolean isDefault) {
        validateServiceName(name);

        ServiceOffered service = serviceOfferedRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Service not found with ID: " + id));

        if (!name.equals(service.getName()) && serviceOfferedRepository.existsByName(name)) {
            throw new BadRequestException("Service with name '" + name + "' already exists.");
        }

        service.setName(name);
        service.setDefault(isDefault);
        ServiceOffered updatedService = serviceOfferedRepository.save(service);
        logger.info("Updated service offered ID {} to name {} (default: {})", id, name, isDefault);
        return updatedService;
    }

    @Override
    @Transactional
    public void deleteServiceOffered(Long id) {
        ServiceOffered service = serviceOfferedRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Service not found with ID: " + id));

        serviceOfferedRepository.delete(service);
        logger.info("Deleted service offered ID {}", id);
    }

    @Override
    public ServiceOffered getServiceOfferedById(Long id) {
        return serviceOfferedRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Service not found with ID: " + id));
    }
    @Override
    public Page<ServiceOffered> getAllServicesOffered(String name, Boolean isDefault, Pageable pageable) {
        Page<ServiceOffered> services = serviceOfferedRepository.findByNameAndIsDefault(
                name != null && !name.trim().isEmpty() ? name.trim() : null,
                isDefault,
                pageable
        );
        logger.info("Retrieved {} services with filters name='{}', isDefault={}",
                services.getTotalElements(), name, isDefault);
        return services;
    }

    private void validateServiceName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Service name cannot be null or empty.");
        }
        if (name.length() > 100) {
            throw new BadRequestException("Service name cannot exceed 100 characters.");
        }
    }
}