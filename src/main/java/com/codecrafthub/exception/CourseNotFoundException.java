package com.codecrafthub.exception;

/**
 * Thrown when a requested course ID does not exist.
 */
public class CourseNotFoundException extends RuntimeException {

    public CourseNotFoundException(Long id) {
        super("Course with id " + id + " was not found");
    }
}