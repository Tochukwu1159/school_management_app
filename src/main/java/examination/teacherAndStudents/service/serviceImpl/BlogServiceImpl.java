package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.BlogRequest;
import examination.teacherAndStudents.dto.BlogResponse;
import examination.teacherAndStudents.entity.Blog;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.BlogRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.BlogService;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Override
    public Page<BlogResponse> getAllBlogPosts(
            String title,
            Long schoolId,
            Long authorId,
            LocalDateTime createdAtStart,
            LocalDateTime createdAtEnd,
            Long id,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        try {
            // Create Pageable object
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Blog> blogsPage = blogRepository.findAllWithFilters(
                    title,
                    schoolId,
                    authorId,
                    createdAtStart,
                    createdAtEnd,
                    id,
                    pageable);

            return blogsPage.map(BlogResponse::fromEntity);
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while fetching blog posts: " + e.getMessage());
        }
    }

    @Override
    public BlogResponse getBlogPostById(Long id) {
        return blogRepository.findById(id)
                .map(BlogResponse::fromEntity)
                .orElseThrow(() -> new NotFoundException("Blog not found "));
    }

    @Override
    @Transactional
    public BlogResponse createBlogPost(BlogRequest blogRequest) {
        Profile admin = getAuthenticatedAdmin();
        Blog newBlog = Blog.builder()
                .title(blogRequest.getTitle())
                .content(blogRequest.getContent())
                .imageUrl(blogRequest.getImageUrl())
                .school(admin.getUser().getSchool())
                .author(admin)
                .build();
        return BlogResponse.fromEntity(blogRepository.save(newBlog));
    }

    @Override
    @Transactional
    public BlogResponse updateBlogPost(Long id, BlogRequest blogRequest) {
        Profile admin = getAuthenticatedAdmin();
        Blog existingBlog = blogRepository.findByIdAndSchool(id, admin.getUser().getSchool())
                .orElseThrow(() -> new NotFoundException("Blog not found "));

        existingBlog.updateDetails(
                blogRequest.getTitle(),
                blogRequest.getContent(),
                blogRequest.getImageUrl()
        );

        return BlogResponse.fromEntity(blogRepository.save(existingBlog));
    }

    @Override
    @Transactional
    public String deleteBlogPost(Long id) {
        Profile admin = getAuthenticatedAdmin();
        Blog blog = blogRepository.findByIdAndSchool(id, admin.getUser().getSchool())
                .orElseThrow(() -> new NotFoundException("Blog not found "));
        blogRepository.delete(blog);
        return "Blog deleted successfully";
    }

    private Profile getAuthenticatedAdmin() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User user = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new NotFoundException("User not found "));
        return profileRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("profile not found "));
    }
}