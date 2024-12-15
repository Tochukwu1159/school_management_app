package examination.teacherAndStudents.controller;

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

    private BusRouteService routeService;

    @PostMapping
    public ResponseEntity<RouteResponse> addRoute(@RequestBody RouteRequest routeRequest) {
        RouteResponse routeResponse = routeService.createRoute(routeRequest);
        return new ResponseEntity<>(routeResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteResponse> updateRoute(@PathVariable Long id, @RequestBody RouteRequest routeRequest) {
        RouteResponse routeResponse = routeService.updateRoute(id, routeRequest);
        return new ResponseEntity<>(routeResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
        public ResponseEntity<Page<RouteResponse>> getAllRoutes(@RequestParam(defaultValue = AccountUtils.PAGENO) Integer pageNo,
                @RequestParam(defaultValue = AccountUtils.PAGESIZE) Integer pageSize,
                @RequestParam(defaultValue = "id") String sortBy) {
        Page<RouteResponse> routes = routeService.getAllRoutes(pageNo, pageSize, sortBy);
        return new ResponseEntity<>(routes, HttpStatus.OK);
    }
}
