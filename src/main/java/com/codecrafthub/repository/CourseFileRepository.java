package com.codecrafthub.repository;
import com.codecrafthub.model.Course;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CourseFileRepository {

    private final ObjectMapper objectMapper;
    private final Path filePath = Paths.get("data/courses.json");

    public CourseFileRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public List<Course> findAll() {
        try {
            ensureFileExists();

            String json = Files.readString(filePath);

            if (json.isBlank()) {
                return new ArrayList<>();
            }

            return objectMapper.readValue(
                    json,
                    new TypeReference<List<Course>>() {}
            );

        } catch (IOException e) {
            throw new RuntimeException("Could not read courses file", e);
        }
    }

    public void saveAll(List<Course> courses) {
        try {
            ensureFileExists();

            String json = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(courses);

            Files.writeString(filePath, json);

        } catch (IOException e) {
            throw new RuntimeException("Could not save courses file", e);
        }
    }

    private void ensureFileExists() throws IOException {
        if (Files.notExists(filePath)) {
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, "[]");
        }
    }
}