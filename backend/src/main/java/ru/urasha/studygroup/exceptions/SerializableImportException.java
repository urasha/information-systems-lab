package ru.urasha.studygroup.exceptions;

public class SerializableImportException extends RuntimeException {
    public SerializableImportException() {
        super(ExceptionMessages.FAILED_IMPORT_SERIALIZATION.getMessage());
    }
}
