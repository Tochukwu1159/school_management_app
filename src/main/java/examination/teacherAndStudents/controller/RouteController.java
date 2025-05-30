package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.RouteRequest;
import examination.teacherAndStudents.dto.RouteResponse;
import examination.teacherAndStudents.service.BusRouteService;
import examination.teacherAndStudents.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/routes")
public class RouteController {

    private final BusRouteService routeService;

    @PostMapping
    public ResponseEntity<ApiResponse<RouteResponse>> addRoute(@RequestBody RouteRequest routeRequest) {
        RouteResponse routeResponse = routeService.createRoute(routeRequest);
        ApiResponse<RouteResponse> response = new ApiResponse<>("Route created successfully", true, routeResponse);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteResponse>> updateRoute(@PathVariable Long id, @RequestBody RouteRequest routeRequest) {
        RouteResponse routeResponse = routeService.updateRoute(id, routeRequest);
        ApiResponse<RouteResponse> response = new ApiResponse<>("Route updated successfully", true, routeResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        ApiResponse<Void> response = new ApiResponse<>("Route deleted successfully", true, null);
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RouteResponse>>> getAllRoutes(
            @RequestParam(defaultValue = AccountUtils.PAGENO) Integer pageNo,
            @RequestParam(defaultValue = AccountUtils.PAGESIZE) Integer pageSize,
            @RequestParam(defaultValue = "id") String sortBy) {
        Page<RouteResponse> routes = routeService.getAllRoutes(pageNo, pageSize, sortBy);
        ApiResponse<Page<RouteResponse>> response = new ApiResponse<>("Routes fetched successfully", true, routes);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
