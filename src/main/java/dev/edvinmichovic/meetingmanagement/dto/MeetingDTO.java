package dev.edvinmichovic.meetingmanagement.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * MeetingDTO is record required for Data Transfer Object Design Pattern.
 * In that particular case, such record allows to successfully validate the data,
 * required for the Meeting class.
 * @param name
 * @param responsiblePerson
 * @param description
 * @param meetingCategory
 * @param meetingType
 * @param startDate
 * @param endDate
 */
public record MeetingDTO(

        @NotBlank(message = "Meeting must contain a name.")
        String name,
        @NotBlank(message = "A responsible Person for the meeting has to be set.")
        String responsiblePerson,
        String description,
        @NotBlank
        @Pattern(regexp= "^(CodeMonkey|Hub|Short|TeamBuilding)$",
                message = "For the meeting category only CodeMonkey, Hub, Short, or TeamBuilding values are accepted.")
        String meetingCategory,
        @NotBlank
        @Pattern(regexp = "^(Live|InPerson)$",
                message = "For the meeting type only Live or InPerson values are accepted.")
        String meetingType,
        @NotNull
        @FutureOrPresent(message = "The start date of the meeting should be present or future date.")
        LocalDateTime startDate,
        @Future(message = "The end date of the meeting should be future date. ")
        LocalDateTime endDate,
        HashMap<String, LocalDateTime> participants

) {
}
