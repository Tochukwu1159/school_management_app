package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.ClassLevelRequest;
import examination.teacherAndStudents.dto.ClassLevelRequestUrl;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.BadRequestException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.SubscriptionExpiredException;
import examination.teacherAndStudents.error_handler.UnauthorizedException;
import examination.teacherAndStudents.repository.AcademicSessionRepository;
import examination.teacherAndStudents.repository.ClassBlockRepository;
import examination.teacherAndStudents.repository.ClassLevelRepository;
import examination.teacherAndStudents.repository.ClassNameRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.ClassLevelService;
import examination.teacherAndStudents.utils.EntityFetcher;
import examination.teacherAndStudents.utils.Roles;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Validated
public class ClassLevelServiceImpl implements ClassLevelService {

    private static final Logger logger = LoggerFactory.getLogger(ClassLevelServiceImpl.class);

    private final ClassLevelRepository classLevelRepository;
    private final UserRepository userRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final ClassBlockRepository classBlockRepository;
    private final ClassNameRepository classNameRepository;
    private final EntityFetcher entityFetcher;

    @Transactional(readOnly = true)
    @Override
    public Page<ClassLevel> getAllClassLevels(
            Long classLevelId, String className, int page, int size, String sortBy, String sortDirection) {
        User admin = verifyAdminAccess();
        logger.info("Admin {} fetching class levels, classLevelId: {}, className: {}",
                admin.getEmail(), classLevelId, className);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ClassLevel> classLevels = classLevelRepository.findAllWithFilters(
                classLevelId, className, admin.getSchool().getId(), pageable);
        logger.debug("Retrieved {} class levels for school ID: {}", classLevels.getTotalElements(), admin.getSchool().getId());
        return classLevels;
    }

    @Transactional(readOnly = true)
    @Override
    public ClassLevel getClassLevelById(Long id) {
        User admin = verifyAdminAccess();
        logger.info("Admin {} fetching class level ID: {}", admin.getEmail(), id);

        ClassLevel classLevel = classLevelRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Class level not found with ID: " + id));

        if (!classLevel.getSchool().equals(admin.getSchool())) {
            logger.warn("Admin {} attempted to access class level ID: {} from another school", admin.getEmail(), id);
            throw new UnauthorizedException("Cannot access class level from another school");
        }

        logger.debug("Retrieved class level ID: {}", id);
        return classLevel;
    }

    @Transactional
    @Override
    public ClassLevel createClassLevel(ClassLevelRequest classLevelRequest) {
        User admin = verifyAdminAccess();
        logger.info("Admin {} creating class level with classNameId: {} and block range: {}",
                admin.getEmail(), classLevelRequest.getClassNameId(), classLevelRequest.getClassBlocks());

        ClassName className = entityFetcher.fetchClassName(classLevelRequest.getClassNameId());


        Optional<ClassLevel> existingClassLevel = classLevelRepository.findByClassNameIdAndSchoolId(className.getId(), admin.getSchool().getId());
        if (existingClassLevel.isPresent()) {
            throw new BadRequestException("Class level already exists in this school");
        }


        ClassLevel classLevel = ClassLevel.builder()
                .className(className)
                .school(admin.getSchool())
                .build();

        ClassLevel savedClassLevel = classLevelRepository.save(classLevel);
        logger.debug("Created class level ID: {}", savedClassLevel.getId());

        if (classLevelRequest.getClassBlocks() != null && classLevelRequest.getClassBlocks().size() == 2) {
            String startBlock = classLevelRequest.getClassBlocks().get(0);
            String endBlock = classLevelRequest.getClassBlocks().get(1);

            // Validate that inputs are single letters and startBlock comes before endBlock
            if (!startBlock.matches("[A-Z]") || !endBlock.matches("[A-Z]") || startBlock.compareTo(endBlock) >= 0) {
                throw new IllegalArgumentException("Class blocks must be a valid range of single letters (e.g., ['A','E'])");
            }

            // Generate blocks from start to end (inclusive)
            for (char block = startBlock.charAt(0); block <= endBlock.charAt(0); block++) {
                ClassBlock classBlock = ClassBlock.builder()
                        .classLevel(savedClassLevel)
                        .name(className.getName() + "-" + block)
                        .school(savedClassLevel.getSchool())
                        .build();
                classBlockRepository.save(classBlock);
                logger.debug("Created class block: {} for class level ID: {}", classBlock.getName(), savedClassLevel.getId());
            }
        } else if (classLevelRequest.getClassBlocks() != null && classLevelRequest.getClassBlocks().isEmpty()) {
            // If classBlocks is empty, use the className as the block name
            ClassBlock classBlock = ClassBlock.builder()
                    .classLevel(savedClassLevel)
                    .name(className.getName())
                    .school(savedClassLevel.getSchool())
                    .build();
            classBlockRepository.save(classBlock);
            logger.debug("Created class block: {} for class level ID: {}", classBlock.getName(), savedClassLevel.getId());
        } else {
            throw new IllegalArgumentException("Class blocks must be either empty or a range specified as exactly two letters (e.g., ['A','E'])");
        }

        return savedClassLevel;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ClassBlock> getSubClassesByClassLevelId(Long classLevelId) {
        User admin = verifyAdminAccess();
        logger.info("Admin {} fetching subclasses for class level ID: {}", admin.getEmail(), classLevelId);

        ClassLevel classLevel = classLevelRepository.findById(classLevelId)
                .orElseThrow(() -> new CustomNotFoundException("Class level not found with ID: " + classLevelId));

        if (!classLevel.getSchool().equals(admin.getSchool())) {
            logger.warn("Admin {} attempted to access class level ID: {} from another school", admin.getEmail(), classLevelId);
            throw new UnauthorizedException("Cannot access class level from another school");
        }

        List<ClassBlock> subClasses = classBlockRepository.findByClassLevel(classLevel);
        logger.debug("Retrieved {} subclasses for class level ID: {}", subClasses.size(), classLevelId);
        return subClasses;
    }

    @Transactional
    @Override
    public ClassLevel updateClassLevel(Long id, ClassLevelRequest classLevelRequest) {
        User admin = verifyAdminAccess();
        logger.info("Admin {} updating class level ID: {} with classNameId: {} and blocks: {}", admin.getEmail(), id,
                classLevelRequest.getClassNameId(), classLevelRequest.getClassBlocks());

        ClassLevel classLevel = classLevelRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Class level not found with ID: " + id));

        if (!classLevel.getSchool().equals(admin.getSchool())) {
            logger.warn("Admin {} attempted to update class level ID: {} from another school", admin.getEmail(), id);
            throw new UnauthorizedException("Cannot update class level from another school");
        }

        ClassName className = entityFetcher.fetchClassName(classLevelRequest.getClassNameId());

        classLevel.setClassName(className);

        // Update class blocks
        if (classLevelRequest.getClassBlocks() != null && !classLevelRequest.getClassBlocks().isEmpty()) {
            List<ClassBlock> existingBlocks = classBlockRepository.findByClassLevel(classLevel);
            Map<String, ClassBlock> existingBlockMap = existingBlocks.stream()
                    .collect(Collectors.toMap(ClassBlock::getName, Function.identity()));

            // Create or update blocks
            for (String block : classLevelRequest.getClassBlocks()) {
                String blockName = className.getName() + "-" + block;
                ClassBlock classBlock = existingBlockMap.getOrDefault(blockName, ClassBlock.builder()
                        .classLevel(classLevel)
                        .name(blockName)
                        .build());
                classBlockRepository.save(classBlock);
                logger.debug("Updated/Created class block: {} for class level ID: {}", blockName, id);
                existingBlockMap.remove(blockName); // Remove processed block
            }

            // Delete blocks that are no longer needed
            classBlockRepository.deleteAll(existingBlockMap.values());
            logger.debug("Deleted {} unused class blocks for class level ID: {}", existingBlockMap.size(), id);
        }

        ClassLevel updatedClassLevel = classLevelRepository.save(classLevel);
        logger.debug("Updated class level ID: {}", id);
        return updatedClassLevel;
    }

    @Transactional
    @Override
    public ClassBlock updateClassBlockUrl(Long classBlockId, @Valid ClassLevelRequestUrl classLevelRequestUrl) {
        User admin = verifyAdminAccess();
        logger.info("Admin {} updating class block URL for ID: {}", admin.getEmail(), classBlockId);

        ClassBlock classBlock = classBlockRepository.findById(classBlockId)
                .orElseThrow(() -> new CustomNotFoundException("Class block not found with ID: " + classBlockId));

        if (!classBlock.getClassLevel().getSchool().equals(admin.getSchool())) {
            logger.warn("Admin {} attempted to update class block ID: {} from another school", admin.getEmail(), classBlockId);
            throw new UnauthorizedException("Cannot update class block from another school");
        }

        classBlock.setClassUniqueUrl(classLevelRequestUrl.getClassUniqueUrl());
        ClassBlock updatedClassBlock = classBlockRepository.save(classBlock);
        logger.debug("Updated class block URL for ID: {}", classBlockId);
        return updatedClassBlock;
    }

    @Transactional
    @Override
    public void deleteClassLevel(Long id) {
        User admin = verifyAdminAccess();
        logger.info("Admin {} deleting class level ID: {}", admin.getEmail(), id);

        ClassLevel classLevel = classLevelRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Class level not found with ID: " + id));

        if (!classLevel.getSchool().equals(admin.getSchool())) {
            logger.warn("Admin {} attempted to delete class level ID: {} from another school", admin.getEmail(), id);
            throw new UnauthorizedException("Cannot delete class level from another school");
        }

        classLevelRepository.delete(classLevel);
        logger.debug("Deleted class level ID: {}", id);
    }

    private void validateSubscription(School school) {
        if (school == null || !school.isSubscriptionValid()) {
            logger.warn("Operation blocked for school due to expired subscription");
            throw new SubscriptionExpiredException("Active school subscription required");
        }
    }

    private User verifyAdminAccess() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found with email: " + email));
        if (!user.getRoles().contains(Roles.ADMIN)) {
            logger.warn("Unauthorized access attempt by user: {}", email);
            throw new UnauthorizedException("Access restricted to ADMIN role");
        }
        if (user.getSchool() == null) {
            logger.warn("User {} not associated with a school", email);
            throw new CustomNotFoundException("User not associated with a school");
        }
        return user;
    }
}