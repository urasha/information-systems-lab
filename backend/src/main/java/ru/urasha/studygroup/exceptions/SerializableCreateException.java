package ru.urasha.studygroup.exceptions;

public class SerializableCreateException extends RuntimeException {
    public SerializableCreateException() {
        super(ExceptionMessages.FAILED_CREATE_SERIALIZATION.getMessage());
    }
}
