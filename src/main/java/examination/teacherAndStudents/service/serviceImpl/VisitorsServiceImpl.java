package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.VisitorsRequest;
import examination.teacherAndStudents.dto.VisitorsResponse;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.Visitors;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.VisitorsRepository;
import examination.teacherAndStudents.service.VisitorsService;
import examination.teacherAndStudents.utils.VisitorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static examination.teacherAndStudents.utils.VisitorStatus.CHECKED_IN;


@Service
@RequiredArgsConstructor
public class VisitorsServiceImpl implements VisitorsService {

    private final VisitorsRepository visitorsRepository;
    private final ProfileRepository profileRepository;

    @Override
    public VisitorsResponse addVisitor(VisitorsRequest request) {
        Profile profile = profileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new NotFoundException("Visitor not found"));

        Visitors visitor = Visitors.builder()
                .name(request.getName())
                .numOfPeople(request.getNumberOfPeople())
                .purpose(request.getPurpose())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .status(CHECKED_IN)
                .host(profile)
                .visitorType(request.getVisitorType())
                .build();

        return mapToResponse(visitorsRepository.save(visitor));
    }


    @Override
    public VisitorsResponse editVisitor(Long id, VisitorsRequest request) {
        try {
            Visitors visitor = visitorsRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Visitor not found"));

            Profile profile = profileRepository.findById(request.getProfileId())
                    .orElseThrow(() -> new NotFoundException("Profile not found"));

            visitor.setHost(profile);
            Optional.ofNullable(request.getName()).ifPresent(visitor::setName);
            Optional.ofNullable(request.getPhoneNumber()).ifPresent(visitor::setPhoneNumber);
            Optional.ofNullable(request.getEmail()).ifPresent(visitor::setEmail);
            Optional.ofNullable(request.getVisitorType()).ifPresent(visitor::setVisitorType);
            Optional.ofNullable(request.getPurpose()).ifPresent(visitor::setPurpose);

            return mapToResponse(visitorsRepository.save(visitor));

        } catch (NotFoundException e) {
            throw e; // Re-throw NotFoundException as it's a specific error
        } catch (Exception e) {
            throw new CustomInternalServerException("Error occurred while editing visitor "+ e);
        }
    }

    @Override
    public void deleteVisitor(Long id) {
        try {
            visitorsRepository.deleteById(id);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error occurred while deleting visitor " +e);
        }
    }

    public Page<VisitorsResponse> getAllVisitors(
            String name,
            String phoneNumber,
            String email,
            VisitorStatus status,
            int pageNo,
            int pageSize,
            String sortBy,
            String sortDirection) {

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable paging = PageRequest.of(pageNo, pageSize, sort);

            Page<Visitors> visitorsPage = visitorsRepository.findAllWithFilters(
                    name,
                    phoneNumber,
                    email,
                    status,
                    paging);

            return visitorsPage.map(this::mapToResponse);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error occurred while fetching visitors: " + e.getMessage());
        }
    }

    private VisitorsResponse mapToResponse(Visitors visitor) {
        VisitorsResponse response = new VisitorsResponse();
        response.setId(visitor.getId());
        response.setName(visitor.getName());
        response.setVisitorType(visitor.getVisitorType());
        response.setEmail(visitor.getEmail());
        response.setStatus(visitor.getStatus());
        response.setPhoneNumber(visitor.getPhoneNumber());
        response.setSignOut(visitor.getSignOut());
        response.setSignIn(visitor.getSignIn());
        response.setPurpose(visitor.getPurpose());
        return response;
    }
}

