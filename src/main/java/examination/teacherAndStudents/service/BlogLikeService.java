package examination.teacherAndStudents.service;

public interface BlogLikeService {
    boolean toggleLikeBlog(Long blogId);
    long getBlogLikeCount(Long blogId);
}
