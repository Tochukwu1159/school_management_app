package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.LibraryMemberResponse;
import examination.teacherAndStudents.dto.LibraryMembershipRequest;
import org.springframework.data.domain.Page;

public interface LibraryMemberService {
    LibraryMemberResponse createLibraryMember(LibraryMembershipRequest request);
    LibraryMemberResponse updateLibraryMember(Long id, LibraryMembershipRequest request);
    LibraryMemberResponse findById(Long id);
    Page<LibraryMemberResponse> findAll(int pageNo, int pageSize, String sortBy);
    void deleteLibraryMember(Long id);
}