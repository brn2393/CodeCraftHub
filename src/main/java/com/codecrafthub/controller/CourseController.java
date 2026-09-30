package com.codecrafthub.controller;

import com.codecrafthub.model.Course;
import com.codecrafthub.model.CourseStatus;
import com.codecrafthub.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for course CRUD operations.
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * POST /api/courses
     *
     * Adds a new course.
     */
    @PostMapping
    public ResponseEntity<Course> createCourse(
            @Valid @RequestBody Course course
    ) {
        Course createdCourse = courseService.createCourse(course);

        URI location = URI.create(
                "/api/courses/" + createdCourse.getId()
        );

        return ResponseEntity
                .created(location)
                .body(createdCourse);
    }

    /**
     * GET /api/courses
     *
     * Returns all courses.
     */
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    /**
     * GET /api/courses/stats
     *
     * Returns the total number of courses and a count for each status.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCourseStats() {
        List<Course> courses = courseService.getAllCourses();
        Map<String, Long> coursesByStatus = new LinkedHashMap<>();

        for (CourseStatus status : CourseStatus.values()) {
            long count = courses.stream()
                    .filter(course -> course.getStatus() == status)
                    .count();
            coursesByStatus.put(status.getValue(), count);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCourses", courses.size());
        stats.put("coursesByStatus", coursesByStatus);

        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/courses/{id}
     *
     * Returns one course.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    /**
     * PUT /api/courses/{id}
     *
     * Replaces the editable information for a course.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody Course course
    ) {
        return ResponseEntity.ok(
                courseService.updateCourse(id, course)
        );
    }

    /**
     * DELETE /api/courses/{id}
     *
     * Deletes a course.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long id
    ) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}