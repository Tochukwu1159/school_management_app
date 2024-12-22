package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.LibraryMemberResponse;
import examination.teacherAndStudents.dto.LibraryMembershipRequest;
import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.LibraryMembership;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.EntityNotFoundException;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.repository.ClassBlockRepository;
import examination.teacherAndStudents.repository.LibraryMemberRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.service.LibraryMemberService;
import examination.teacherAndStudents.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LibraryMemberServiceImpl implements LibraryMemberService {

    private final LibraryMemberRepository libraryMemberRepository;
    private final ProfileRepository profileRepository;
    private final ClassBlockRepository subClassRepository;

    @Override
    public LibraryMemberResponse createLibraryMember(LibraryMembershipRequest request) {
        try {
            Profile student = profileRepository.findByUniqueRegistrationNumber(request.getUserUniqueRegistrationNumber())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with unique registration number: " + request.getUserUniqueRegistrationNumber()));

            ClassBlock studentClass = subClassRepository.findById(request.getUserClassId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User class not found with ID: " + request.getUserClassId()));

            String membershipId = AccountUtils.generateLibraryId();
            LibraryMembership libraryMember = new LibraryMembership();
            libraryMember.setStudent(student);
            libraryMember.setMemberId(membershipId);

            LibraryMembership savedMember = libraryMemberRepository.save(libraryMember);
            return mapToLibraryMemberResponse(savedMember);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error creating library member: " + e.getMessage());
        }
    }

    @Override
    public LibraryMemberResponse updateLibraryMember(Long memberId, LibraryMembershipRequest request) {
        try {
            LibraryMembership existingMember = libraryMemberRepository.findById(memberId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Library membership not found with ID: " + memberId));

            LibraryMembership updatedMember = libraryMemberRepository.save(existingMember);

            return mapToLibraryMemberResponse(updatedMember);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error updating library member: " + e.getMessage());
        }
    }

    @Override
    public LibraryMemberResponse findById(Long id) {
        LibraryMembership libraryMember = libraryMemberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Library member not found with ID: " + id));
        return mapToLibraryMemberResponse(libraryMember);
    }

    @Override
    public Page<LibraryMemberResponse> findAll(int pageNo, int pageSize, String sortBy) {
        try {
            sortBy = (sortBy == null || sortBy.isEmpty()) ? "id" : sortBy; // Default sorting by `id`
            Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
            Page<LibraryMembership> libraryMembers = libraryMemberRepository.findAll(paging);
            return libraryMembers.map(this::mapToLibraryMemberResponse);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching library members: " + e.getMessage());
        }
    }

    @Override
    public void deleteLibraryMember(Long id) {
        try {
            if (!libraryMemberRepository.existsById(id)) {
                throw new ResourceNotFoundException("Library member not found with ID: " + id);
            }
            libraryMemberRepository.deleteById(id);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error deleting library member: " + e.getMessage());
        }
    }

    private LibraryMemberResponse mapToLibraryMemberResponse(LibraryMembership libraryMember) {
        return LibraryMemberResponse.builder()
                .id(libraryMember.getId())
                .memberId(libraryMember.getMemberId())
                .build();
    }
}
