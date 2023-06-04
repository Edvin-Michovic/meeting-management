package dev.edvinmichovic.meetingmanagement.controller;

import dev.edvinmichovic.meetingmanagement.dto.MeetingDTO;
import dev.edvinmichovic.meetingmanagement.model.Meeting;
import dev.edvinmichovic.meetingmanagement.repository.MeetingRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/meetings")
@Validated
public class MeetingController {

    private final MeetingRepository repository;

    public MeetingController(MeetingRepository repository) {
        this.repository = repository;
    }

    /**
     * GET http://localhost:8080/meetings
     * Endpoint lists all present meetings.
     * Different filters can be applied.
     */
    @GetMapping("")
    public List<Meeting> findAll(
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String responsiblePerson,
            @RequestParam(required = false)
            @Pattern(regexp = "^(CodeMonkey|Hub|Short|TeamBuilding)$",
                    message = "For the meeting category only CodeMonkey, Hub, Short, or TeamBuilding values are accepted.")
            String category,
            @RequestParam(required = false)
            @Pattern(regexp = "^(Live|InPerson)$",
                    message = "For the meeting type only Live or InPerson values are accepted.")
            String type,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @Min(value = 0, message = "Minimal value of attendees should be positive.") @RequestParam(required = false) Integer minAttendees
    ) {
        return repository.findAll(description, responsiblePerson, category, type, startDate, endDate, minAttendees);
    }

    /**
     * GET http://localhost:8080/meetings/{name}
     * Endpoint allows to find the meetings by its name.
     * Of course, if the meeting exists.
     * In case if meeting is not found, the following message will be sent.
     */
    @GetMapping("/{name}")
    public Meeting findByName(@PathVariable String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting with that name was not found."));
    }

    /**
     * POST http://localhost:8080/meetings - create new meeting
     * In the body of the POST request such variables are necessary:
     * name, responsiblePerson, meetingCategory, meetingType, startDate.
     * startDate and endDate should be Present or Future.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("")
    public void create(@Valid @RequestBody MeetingDTO meetingDTO) {
        repository.save(meetingDTO);
    }

    /**
     * DELETE http://localhost:8080/meetings/{name}?responsiblePerson={responsiblePerson}
     * Endpoint allows to delete the meeting.
     * The meeting will be deleted only if responsible person is specified.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{name}")
    public void delete(@PathVariable String name, @RequestParam String responsiblePerson) {
        if (!repository.delete(name, responsiblePerson)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Only responsible person can delete the meeting.\n" +
                    "Please make sure the responsible person is correct for this meeting.");
        }
    }

    /**
     * PUT http://localhost:8080/meetings/{name}/addParticipant
     * Endpoint allows to add participant(s) to the meeting.
     * The user will be warned if any user from the list is already added to the meeting.
     */
    @PutMapping("/{name}/addParticipant")
    public ResponseEntity<String> addParticipant(@PathVariable String name, @RequestBody List<String> participants) {
        if (participants == null || participants.isEmpty()) {
            return ResponseEntity.badRequest().body("Participants list is empty.");
        }
        if (!repository.addParticipant(name, participants).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CREATED).body("WARNING! " + participants +
                    " participant are already invited.\n" +
                    "Already invited participant(s) won't be added to the meeting's participants list.\n" +
                    "All other users that were not present in the meeting before will be added.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully added participants.");
    }

    /**
     * DELETE http://localhost:8080/meetings/{name}/removeParticipant
     * Endpoint allows to remove participant(s) from the meeting.
     * If there is a responsible person in the list, he/she won't be removed.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{name}/removeParticipant")
    public ResponseEntity<String> removeParticipant(@PathVariable String name, @RequestBody List<String> participants) {
        if (participants == null || participants.isEmpty()) {
            return ResponseEntity.badRequest().body("Participants list is empty.");
        }
        repository.removeParticipant(name, participants);
        return ResponseEntity.status(200).body("Participants, if they were present, are successfully deleted.\n" +
                "NOTE: Meeting's responsible person won't be deleted from the meeting.");
    }

}
