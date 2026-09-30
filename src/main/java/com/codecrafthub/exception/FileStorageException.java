package com.codecrafthub.exception;

/**
 * Thrown when courses.json cannot be read or written.
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
