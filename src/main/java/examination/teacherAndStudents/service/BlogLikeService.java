package examination.teacherAndStudents.service;

public interface BlogLikeService {
    void toggleLikeBlog(Long blogId);
    long getBlogLikeCount(Long blogId);
}
