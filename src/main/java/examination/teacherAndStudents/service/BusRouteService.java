package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.RouteRequest;
import examination.teacherAndStudents.dto.RouteResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BusRouteService {
    Page<RouteResponse> getAllRoutes(int pageNo, int pageSize, String sortBy);
    RouteResponse getRouteById(Long id);
    RouteResponse createRoute(RouteRequest routeRequest);
    RouteResponse updateRoute(Long id, RouteRequest routeRequest);
    void deleteRoute(Long id);
}
