package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.RouteRequest;
import examination.teacherAndStudents.dto.RouteResponse;
import examination.teacherAndStudents.entity.BusRoute;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.repository.BusRouteRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.BusRouteService;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BusRouteServiceImpl implements BusRouteService {
    private final BusRouteRepository busRouteRepository;
    private final UserRepository userRepository;


    public Page<RouteResponse> getAllRoutes(int pageNo, int pageSize, String sortBy) {

        try {
            Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
            Page<BusRoute> busRoutes = busRouteRepository.findAll(paging);
            return busRoutes.map(this::mapToRouteResponse);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve all routes", e);
        }
    }

    public RouteResponse getRouteById(Long id) {
        try {
            BusRoute busRoute = busRouteRepository.findById(id).orElse(null);
            if (busRoute == null) {
                throw new IllegalArgumentException("Route not found for id: " + id);
            }
            return mapToRouteResponse(busRoute);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve route by id: " + id, e);
        }
    }

    public RouteResponse createRoute(RouteRequest routeRequest) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }

            Optional<User> userDetails = userRepository.findByEmail(email);
            BusRoute newRoute = new BusRoute();
            newRoute.setRouteName(routeRequest.getRouteName());
            newRoute.setStartPoint(routeRequest.getStartPoint());
            newRoute.setEndPoint(routeRequest.getEndPoint());
            newRoute.setSchool(userDetails.get().getSchool());
            BusRoute savedRoute = busRouteRepository.save(newRoute);
            return mapToRouteResponse(savedRoute);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create route", e);
        }
    }

    public RouteResponse updateRoute(Long id, RouteRequest routeRequest) {
        try {
            BusRoute existingRoute = busRouteRepository.findById(id).orElse(null);
            if (existingRoute == null) {
                throw new IllegalArgumentException("Route not found for id: " + id);
            }
            existingRoute.setRouteName(routeRequest.getRouteName());
            existingRoute.setStartPoint(routeRequest.getStartPoint());
            existingRoute.setEndPoint(routeRequest.getEndPoint());
            BusRoute updatedRoute = busRouteRepository.save(existingRoute);
            return mapToRouteResponse(updatedRoute);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update route with id: " + id, e);
        }
    }

    public void deleteRoute(Long id) {
        try {
            busRouteRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete route with id: " + id, e);
        }
    }

    private RouteResponse mapToRouteResponse(BusRoute busRoute) {
        RouteResponse routeResponse = new RouteResponse();
        routeResponse.setId(busRoute.getId());
        routeResponse.setRouteName(busRoute.getRouteName());
        routeResponse.setStartPoint(busRoute.getStartPoint());
        routeResponse.setEndPoint(busRoute.getEndPoint());
        return routeResponse;
    }
}
