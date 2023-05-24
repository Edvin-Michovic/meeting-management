package dev.edvinmichovic.meetingmanagement.model;

import java.time.LocalDateTime;
import java.util.HashMap;

public record Meeting(

        String name,
        String responsiblePerson,
        String description,
        Category meetingCategory,
        Type meetingType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        HashMap<String, LocalDateTime> participants
) {

}
