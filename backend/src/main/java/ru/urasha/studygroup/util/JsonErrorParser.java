package ru.urasha.studygroup.util;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.core.JsonLocation;
import ru.urasha.studygroup.dto.ErrorDto;

import java.util.List;

public final class JsonErrorParser {

    private JsonErrorParser() { }

    public static ErrorDto fromJsonMappingException(JsonMappingException jme) {
        List<JsonMappingException.Reference> path = jme.getPath();
        StringBuilder fieldBuilder = new StringBuilder();
        int recordIndex = -1;

        for (JsonMappingException.Reference ref : path) {
            if (ref.getFieldName() != null) {
                if (!fieldBuilder.isEmpty()) fieldBuilder.append('.');
                fieldBuilder.append(ref.getFieldName());
            }
            if (ref.getIndex() != -1 && recordIndex == -1) {
                recordIndex = ref.getIndex();
            }
        }

        String fieldPath = fieldBuilder.isEmpty() ? "file" : fieldBuilder.toString();

        if ("file".equals(fieldPath)) {
            JsonLocation loc = jme.getLocation();
            if (loc != null && loc.getLineNr() >= 0) {
                String msg = String.format("Invalid JSON near line %d, column %d",
                        loc.getLineNr(), loc.getColumnNr());
                return new ErrorDto(recordIndex, "file", msg);
            } else {
                return new ErrorDto(recordIndex, "file", "Invalid JSON structure or value");
            }
        }

        String message = "Invalid value for field '" + fieldPath + "'";
        return new ErrorDto(recordIndex, fieldPath, message);
    }
}
