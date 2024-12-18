package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.VisitorsRequest;
import examination.teacherAndStudents.dto.VisitorsResponse;
import examination.teacherAndStudents.entity.Visitors;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.VisitorsRepository;
import examination.teacherAndStudents.service.VisitorsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static examination.teacherAndStudents.utils.VisitorStatus.CHECKED_IN;


@Service
@RequiredArgsConstructor
public class VisitorsServiceImpl implements VisitorsService {

    private final VisitorsRepository visitorsRepository;

    @Override
    public VisitorsResponse addVisitor(VisitorsRequest request) {
        try {
            Visitors visitor = new Visitors();
            visitor.setName(request.getName());
            visitor.setPurpose(request.getPurpose());
            visitor.setPhoneNumber(request.getPhoneNumber());
            visitor.setEmail(request.getEmail());
            visitor.setStatus(CHECKED_IN);
            visitor.setHostName(request.getHostName());
            visitor.setVisitorType(request.getVisitorType());
            Visitors savedVisitor = visitorsRepository.save(visitor);
            return mapToResponse(savedVisitor);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error occurred while adding visitor "+ e);
        }
    }

    @Override
    public VisitorsResponse editVisitor(Long id, VisitorsRequest request) {
        try {
            Visitors visitor = visitorsRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Visitor not found"));
            visitor.setName(request.getName());
            visitor.setPhoneNumber(request.getPhoneNumber());
           visitor.setEmail(request.getEmail());
           visitor.setVisitorType(request.getVisitorType());
            visitor.setHostName(request.getHostName());
            visitor.setPurpose(request.getPurpose());
            Visitors updatedVisitor = visitorsRepository.save(visitor);
            return mapToResponse(updatedVisitor);
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

    @Override
    public Page<VisitorsResponse> getAllVisitors(int pageNo, int pageSize, String sortBy) {
        try {
            Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
            Page<Visitors> visitorsPage = visitorsRepository.findAll(paging);
            return visitorsPage.map(this::mapToResponse);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error occurred while fetching all visitors " +e);
        }
    }

    private VisitorsResponse mapToResponse(Visitors visitor) {
        VisitorsResponse response = new VisitorsResponse();
        response.setId(visitor.getId());
        response.setName(visitor.getName());
        response.setHostName(visitor.getHostName());
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

