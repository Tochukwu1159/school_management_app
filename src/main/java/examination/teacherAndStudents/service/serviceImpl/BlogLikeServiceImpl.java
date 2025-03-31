package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.entity.Blog;
import examination.teacherAndStudents.entity.BlogLike;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.BlogLikeRepository;
import examination.teacherAndStudents.repository.BlogRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.BlogLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlogLikeServiceImpl implements BlogLikeService {
    private final BlogLikeRepository blogLikeRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Override
    public void toggleLikeBlog(Long blogId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile user = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found"));
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new CustomNotFoundException("Blog not found"));

        Optional<BlogLike> existingLike = blogLikeRepository.findByBlogAndUser(blog, user);

        if (existingLike.isPresent()) {
            blogLikeRepository.delete(existingLike.get());
        } else {
            BlogLike like = new BlogLike(null, blog, user, LocalDateTime.now());
            blogLikeRepository.save(like);
        }
    }

    @Override
    public long getBlogLikeCount(Long blogId) {
        return blogLikeRepository.count();
    }
}
