package com.codecrafthub.service;

import com.codecrafthub.exception.CourseNotFoundException;
import com.codecrafthub.exception.FileStorageException;
import com.codecrafthub.model.Course;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer responsible for course operations and JSON file storage.
 *
 * This implementation stores every course in a file called courses.json.
 */
@Service
public class CourseService {

    private static final Path COURSES_FILE = Paths.get("courses.json");

    private final ObjectMapper objectMapper;

    public CourseService() {
        /*
         * ObjectMapper is Jackson's main JSON processing class.
         */
        this.objectMapper = new ObjectMapper();

        /*
         * Register support for LocalDate and Instant.
         */
        this.objectMapper.registerModule(new JavaTimeModule());

        /*
         * Write dates in readable ISO formats instead of numeric timestamps.
         */
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Returns every course from courses.json.
     */
    public synchronized List<Course> getAllCourses() {
        return readCoursesFromFile();
    }

    /**
     * Returns one course by ID.
     */
    public synchronized Course getCourseById(Long id) {
        return readCoursesFromFile()
                .stream()
                .filter(course -> course.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new CourseNotFoundException(id));
    }

    /**
     * Creates and saves a new course.
     *
     * IDs start at 1 and increase based on the largest existing ID.
     */
    public synchronized Course createCourse(Course course) {
        List<Course> courses = readCoursesFromFile();

        long nextId = courses.stream()
                .map(Course::getId)
                .filter(id -> id != null)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1L;

        course.setId(nextId);

        /*
         * Always generate the timestamp on the server.
         */
        course.setCreatedAt(Instant.now());

        courses.add(course);
        writeCoursesToFile(courses);

        return course;
    }

    /**
     * Replaces all editable fields of an existing course.
     *
     * The ID and created_at values are preserved.
     */
    public synchronized Course updateCourse(Long id, Course updatedCourse) {
        List<Course> courses = readCoursesFromFile();

        Course existingCourse = courses.stream()
                .filter(course -> course.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new CourseNotFoundException(id));

        existingCourse.setName(updatedCourse.getName());
        existingCourse.setDescription(updatedCourse.getDescription());
        existingCourse.setTargetDate(updatedCourse.getTargetDate());
        existingCourse.setStatus(updatedCourse.getStatus());

        writeCoursesToFile(courses);

        return existingCourse;
    }

    /**
     * Deletes a course by ID.
     */
    public synchronized void deleteCourse(Long id) {
        List<Course> courses = readCoursesFromFile();

        boolean removed = courses.removeIf(course -> course.getId().equals(id));

        if (!removed) {
            throw new CourseNotFoundException(id);
        }

        writeCoursesToFile(courses);
    }

    /**
     * Reads courses.json and converts its JSON content into Course objects.
     */
    private List<Course> readCoursesFromFile() {
        ensureCoursesFileExists();

        try {
            String json = Files.readString(COURSES_FILE);

            /*
             * Treat an empty file as an empty course list.
             */
            if (json.isBlank()) {
                return new ArrayList<>();
            }

            List<Course> courses = objectMapper.readValue(
                    json,
                    new TypeReference<List<Course>>() {
                    }
            );

            if (courses == null) {
                return new ArrayList<>();
            }

            long nextId = courses.stream()
                    .map(Course::getId)
                    .filter(id -> id != null)
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0L) + 1L;
            boolean migrated = false;

            for (Course course : courses) {
                if (course.getId() == null) {
                    course.setId(nextId++);
                    migrated = true;
                }
                if (course.getCreatedAt() == null) {
                    course.setCreatedAt(Instant.now());
                    migrated = true;
                }
            }

            if (migrated) {
                writeCoursesToFile(courses);
            }

            return courses;

        } catch (IOException | RuntimeException exception) {
            throw new FileStorageException(
                    "Could not read courses.json",
                    exception
            );
        }
    }

    /**
     * Converts the course list to formatted JSON and writes it to courses.json.
     */
    private void writeCoursesToFile(List<Course> courses) {
        try {
            String json = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(courses);

            Files.writeString(COURSES_FILE, json);

        } catch (IOException | RuntimeException exception) {
            throw new FileStorageException(
                    "Could not write courses.json",
                    exception
            );
        }
    }

    /**
     * Creates courses.json automatically if it does not already exist.
     */
    private void ensureCoursesFileExists() {
        try {
            if (Files.notExists(COURSES_FILE)) {
                Files.writeString(COURSES_FILE, "[]");
            }
        } catch (IOException exception) {
            throw new FileStorageException(
                    "Could not create courses.json",
                    exception
            );
        }
    }
}