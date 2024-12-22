package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.NoticeRequest;
import examination.teacherAndStudents.dto.NoticeResponse;
import examination.teacherAndStudents.dto.UpdateNoticeRequest;
import examination.teacherAndStudents.entity.Notice;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.NoticeRepository;
import examination.teacherAndStudents.repository.NotificationRepository;
import examination.teacherAndStudents.repository.TransactionRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.NoticeService;
import examination.teacherAndStudents.utils.Roles;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final TransactionRepository transactionRepository;
    private final ModelMapper modelMapper;

    public NoticeServiceImpl(NoticeRepository noticeRepository, UserRepository userRepository,
                             NotificationRepository notificationRepository,
                             TransactionRepository transactionRepository,
                             ModelMapper modelMapper) {
        this.noticeRepository = noticeRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.transactionRepository = transactionRepository;
        this.modelMapper = modelMapper;
    }

    public List<NoticeResponse> getAllNoticePosts() {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }
            return noticeRepository.findAll().stream().map((element) -> modelMapper.map(element, NoticeResponse.class)).collect(Collectors.toList());
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while fetching blog posts " + e.getMessage());
        }
    }

    public NoticeResponse getNoticePostById(Long id) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }

            Notice notice = noticeRepository.findById(id)
                    .orElseThrow(() -> new CustomNotFoundException("Notice post not found with id: " + id));
         return    modelMapper.map(notice, NoticeResponse.class);

        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while fetching the blog post " + e.getMessage());
        }
    }


    public List<NoticeResponse> getEventsByDateRange(LocalDate startDate, LocalDate endDate) {
        return noticeRepository.findByEventDateBetween(startDate, endDate).stream().map((element) -> modelMapper.map(element, NoticeResponse.class)).collect(Collectors.toList());
    }

    public NoticeResponse createNoticePost(NoticeRequest noticeRequest) {
        try {
                String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }
            Notice newNotice = new Notice();
            newNotice.setTitle(noticeRequest.getTitle());
            newNotice.setEventDate(LocalDate.now());
            newNotice.setEventDescription(noticeRequest.getEventDescription());
            return modelMapper.map(noticeRepository.save(newNotice), NoticeResponse.class);
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while creating the blog post " +e.getMessage());
        }
    }

    public NoticeResponse updateNoticePost(Long id, UpdateNoticeRequest updatedNoticePost) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }

            Notice existingNoticePost = noticeRepository.findById(id)
                    .orElseThrow(() -> new CustomNotFoundException("Notice post not found with id: " + id));

            existingNoticePost.setTitle(updatedNoticePost.getTitle());
            existingNoticePost.setEventDescription(updatedNoticePost.getEventDescription());
            existingNoticePost.setEventDate(LocalDate.now());
            // You can update other fields as needed

            return modelMapper.map(noticeRepository.save(existingNoticePost), NoticeResponse.class);
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while updating the blog post" + e.getMessage());
        }
    }


    public boolean deleteNoticePost(Long id) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }

            Notice existingNoticePost = noticeRepository.findById(id)
                    .orElseThrow(() -> new CustomNotFoundException("Notice post not found with id: " + id));

            noticeRepository.delete(existingNoticePost);
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while deleting the blog post " +  e.getMessage());
        }
        return false;
    }



}