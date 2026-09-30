package com.codecrafthub.model;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The only valid course status values.
 *
 * The JSON values are intentionally written exactly as required:
 *
 * "Not Started"
 * "In Progress"
 * "Completed"
 */
public enum CourseStatus {

    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private final String value;

    CourseStatus(String value) {
        this.value = value;
    }

    /**
     * Controls how the enum is written to JSON responses.
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Controls how status strings are read from JSON requests.
     *
     * Only the exact values are accepted.
     */
    @JsonCreator
    public static CourseStatus fromValue(String value) {
        for (CourseStatus status : CourseStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }

        throw new IllegalArgumentException(
                "Invalid status. Allowed values are: "
                        + "\"Not Started\", \"In Progress\", \"Completed\""
        );
    }
}