package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.BlogRequest;
import examination.teacherAndStudents.entity.Blog;
import examination.teacherAndStudents.entity.Notification;
import examination.teacherAndStudents.entity.Transaction;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.BlogRepository;
import examination.teacherAndStudents.repository.NotificationRepository;
import examination.teacherAndStudents.repository.TransactionRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.BlogService;
import examination.teacherAndStudents.utils.NotificationType;
import examination.teacherAndStudents.utils.Roles;
import examination.teacherAndStudents.utils.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final TransactionRepository transactionRepository;

    public BlogServiceImpl(BlogRepository blogRepository, UserRepository userRepository,
                           NotificationRepository notificationRepository,
                           TransactionRepository transactionRepository) {
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Blog> getAllBlogPosts() {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }
            return blogRepository.findAll();
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while fetching blog posts " + e.getMessage());
        }
    }

    public Blog getBlogPostById(Long id) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }

            return blogRepository.findById(id)
                    .orElseThrow(() -> new CustomNotFoundException("Blog post not found with id: " + id));
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while fetching the blog post " + e.getMessage());
        }
    }

    public Blog createBlogPost(BlogRequest blogPost) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }
            Blog newBlog = new Blog();
            newBlog.setContent(blogPost.getContent());
            newBlog.setTitle(blogPost.getTitle());
            return blogRepository.save(newBlog);
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while creating the blog post " +e.getMessage());
        }
    }

    public Blog updateBlogPost(Long id, BlogRequest updatedBlogPost) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }

            Blog existingBlogPost = blogRepository.findById(id)
                    .orElseThrow(() -> new CustomNotFoundException("Blog post not found with id: " + id));

            Date date = new Date();
            LocalDateTime localDateTime = date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            existingBlogPost.setTitle(updatedBlogPost.getTitle());
            existingBlogPost.setContent(updatedBlogPost.getContent());
            existingBlogPost.setCreatedAt(localDateTime);
            // You can update other fields as needed

            return blogRepository.save(existingBlogPost);
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while updating the blog post" + e.getMessage());
        }
    }


    public boolean deleteBlogPost(Long id) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }

            Blog existingBlogPost = blogRepository.findById(id)
                    .orElseThrow(() -> new CustomNotFoundException("Blog post not found with id: " + id));

            blogRepository.delete(existingBlogPost);
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while deleting the blog post " +  e.getMessage());
        }
        return false;
    }
}