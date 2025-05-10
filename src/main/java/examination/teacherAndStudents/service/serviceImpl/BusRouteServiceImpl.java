package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.BusRoute;
import examination.teacherAndStudents.entity.Stop;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.BusRouteRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.BusRouteService;
import examination.teacherAndStudents.utils.AccountUtils;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusRouteServiceImpl implements BusRouteService {
    private final BusRouteRepository busRouteRepository;
    private final UserRepository userRepository;

    @Override
    public Page<RouteResponse> getAllRoutes(int pageNo, int pageSize, String sortBy) {
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
        Page<BusRoute> busRoutes = busRouteRepository.findAll(paging);
        return busRoutes.map(this::mapToRouteResponse);
    }

    @Override
    public RouteResponse getRouteById(Long id) {
        BusRoute busRoute = busRouteRepository.findByIdWithStops(id)
                .orElseThrow(() -> new CustomNotFoundException("Route not found for id: " + id));
        return mapToRouteResponse(busRoute);
    }

    @Override
    @Transactional
    public RouteResponse createRoute(RouteRequest routeRequest) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        AccountUtils.GeocodingResult startCoords = AccountUtils.getCoordinatesFromAddress(routeRequest.getStartPoint());
        AccountUtils.GeocodingResult endCoords = AccountUtils.getCoordinatesFromAddress(routeRequest.getEndPoint());

        BusRoute newRoute = BusRoute.builder()
                .routeName(routeRequest.getRouteName())
                .school(admin.getSchool())
                .startPoint(routeRequest.getStartPoint())
                .startLatitude(startCoords.getLat())
                .startLongitude(startCoords.getLng())
                .endPoint(routeRequest.getEndPoint())
                .endLatitude(endCoords.getLat())
                .endLongitude(endCoords.getLng())
                .build();

        BusRoute savedRoute = busRouteRepository.save(newRoute);
        return mapToRouteResponse(savedRoute);
    }

    @Override
    @Transactional
    public RouteResponse updateRoute(Long id, RouteRequest routeRequest) {
        BusRoute existingRoute = busRouteRepository.findByIdWithStops(id)
                .orElseThrow(() -> new CustomNotFoundException("Route not found for id: " + id));

        AccountUtils.GeocodingResult startCoords = AccountUtils.getCoordinatesFromAddress(routeRequest.getStartPoint());
        AccountUtils.GeocodingResult endCoords = AccountUtils.getCoordinatesFromAddress(routeRequest.getEndPoint());

        existingRoute.setRouteName(routeRequest.getRouteName());
        existingRoute.setStartPoint(routeRequest.getStartPoint());
        existingRoute.setEndPoint(routeRequest.getEndPoint());
        existingRoute.setStartLatitude(startCoords.getLat());
        existingRoute.setStartLongitude(startCoords.getLng());
        existingRoute.setEndLatitude(endCoords.getLat());
        existingRoute.setEndLongitude(endCoords.getLng());

        BusRoute updatedRoute = busRouteRepository.save(existingRoute);
        return mapToRouteResponse(updatedRoute);
    }

    @Override
    @Transactional
    public void deleteRoute(Long id) {
        BusRoute route = busRouteRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Route not found for id: " + id));
        if (!route.getBuses().isEmpty()) {
            throw new IllegalStateException("Cannot delete route with assigned buses");
        }
        busRouteRepository.delete(route);
    }

    private RouteResponse mapToRouteResponse(BusRoute busRoute) {

        return RouteResponse.builder()
                .id(busRoute.getId())
                .routeName(busRoute.getRouteName())
                .startPoint(busRoute.getStartPoint())
                .endPoint(busRoute.getEndPoint())
                .createdAt(busRoute.getCreatedAt())
                .updatedAt(busRoute.getUpdatedAt())
                .build();
    }
}