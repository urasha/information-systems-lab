package ru.urasha.studygroup.exceptions;

import lombok.Getter;

@Getter
public enum ExceptionMessages {

    STUDY_GROUP_NOT_FOUND("StudyGroup with ID: %d not found"),
    SAME_SOURCE_AND_TARGET("Source and target groups must be different"),
    GROUP_WAS_MODIFIED("The group with id %d was modified by another user. Please update the data and try again."),
    FAILED_CREATE_SERIALIZATION("Failed to create StudyGroup due to a serialization conflict"),
    FAILED_IMPORT_SERIALIZATION("Failed to import StudyGroup from file due to a serialization conflict");

    private final String message;

    ExceptionMessages(String message) {
        this.message = message;
    }
}
