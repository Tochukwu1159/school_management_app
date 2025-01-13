package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.RatingRequest;
import examination.teacherAndStudents.dto.RatingResponse;
import examination.teacherAndStudents.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ratings")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @PostMapping
    public RatingResponse createRating(@RequestBody RatingRequest ratingRequest) {
        return ratingService.createRating(ratingRequest);
    }

    @PutMapping("/{ratingId}")
    public RatingResponse updateRating(@PathVariable Long ratingId, @RequestBody RatingRequest ratingRequest) {
        return ratingService.updateRating(ratingId, ratingRequest);
    }

    @DeleteMapping("/{ratingId}")
    public void deleteRating(@PathVariable Long ratingId) {
        ratingService.deleteRatingById(ratingId);
    }

    @GetMapping("/{ratingId}")
    public RatingResponse getRatingById(@PathVariable Long ratingId) {
        return ratingService.getRatingById(ratingId);
    }

    @GetMapping("/school/{schoolId}")
    public List<RatingResponse> findAllRatingsBySchool(@PathVariable Long schoolId) {
        return ratingService.findAllRatingsBySchool(schoolId);
    }
}
