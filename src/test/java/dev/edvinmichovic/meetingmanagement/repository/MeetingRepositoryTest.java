package dev.edvinmichovic.meetingmanagement.repository;

import dev.edvinmichovic.meetingmanagement.dto.MeetingDTO;
import dev.edvinmichovic.meetingmanagement.model.Category;
import dev.edvinmichovic.meetingmanagement.model.Meeting;
import dev.edvinmichovic.meetingmanagement.model.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class MeetingRepositoryTest {

    private MeetingRepository repository = new MeetingRepository();

    @BeforeEach
    public void setBefore() {
        MeetingDTO meeting1 = new MeetingDTO("Meeting 1", "John Doe", "Meeting description",
                "CodeMonkey", "Live", LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                new HashMap<>());
        MeetingDTO meeting2 = new MeetingDTO("Meeting 2", "Jane Smith", "Another meeting description",
                "Hub", "InPerson", LocalDateTime.now(), LocalDateTime.now().plusDays(2),
                new HashMap<>());
        MeetingDTO meeting3 = new MeetingDTO("Meeting 3", "John Doe", "Yet another meeting description",
                "CodeMonkey", "Live", LocalDateTime.now(), LocalDateTime.now().plusDays(3),
                new HashMap<>());

        repository.save(meeting1);
        repository.save(meeting2);
        repository.save(meeting3);

    }


    @Test
    @Order(1)
    void testFindAllWithFilters() {
        List<Meeting> filteredMeetings = repository.findAll("MEETING", "John Doe",
                "CodeMonkey", "Live", LocalDate.parse("2023-05-24"), LocalDate.now().plusDays(3), 0);

        assertEquals(2, filteredMeetings.size());
    }

    @Test
    @Order(2)
    void testSaveNewMeeting() {
        MeetingDTO meeting4 = new MeetingDTO("Meeting 4", "John Doe", "Meeting description",
                "CodeMonkey", "InPerson", LocalDateTime.of(2023, 5, 24, 10, 0),
                LocalDateTime.of(2023, 5, 25, 10, 0),
                new HashMap<>());

        repository.save(meeting4);

        List<Meeting> meetingList = repository.findAll(null, null, null, null, null, null, null);
        assertEquals(4, meetingList.size());

        Meeting savedMeeting = meetingList.get(3);
        assertEquals("Meeting 4", savedMeeting.name());
        assertEquals("John Doe", savedMeeting.responsiblePerson());
        assertEquals("Meeting description", savedMeeting.description());
        assertEquals(Category.CodeMonkey, savedMeeting.meetingCategory());
        assertEquals(Type.InPerson, savedMeeting.meetingType());
        assertEquals(LocalDateTime.of(2023, 5, 24, 10, 0), savedMeeting.startDate());
        assertEquals(LocalDateTime.of(2023, 5, 25, 10, 0), savedMeeting.endDate());
        assertTrue(savedMeeting.participants().containsKey(savedMeeting.responsiblePerson()));
    }

    @Test
    @Order(3)
    void testSaveAlreadyExistingName() {
        MeetingDTO meeting4 = new MeetingDTO("Meeting 1", "John Doe", "Meeting description",
                "CodeMonkey", "Live", LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                new HashMap<>());

        repository.save(meeting4);

        List<Meeting> meetingList = repository.findAll(null, null, null, null, null, null, null);
        assertEquals(3, meetingList.size());
    }

    @Test
    @Order(4)
    void testDeleteValid() {
        boolean deleted1 = repository.delete("Meeting 1", "John Doe");
        boolean deleted2 = repository.delete("Meeting 2", "Jane Smith");
        boolean deleted3 = repository.delete("Meeting 3", "John Doe");

        assertTrue(deleted1);
        assertTrue(deleted2);
        assertTrue(deleted3);
        assertEquals(0, repository.findAll(null, null, null, null, null, null, null).size());
    }

    @Test
    @Order(5)
    void testDeleteInvalidResponsiblePerson() {
        boolean deleted1 = repository.delete("Meeting 1", "Jane Smith");
        boolean deleted2 = repository.delete("Meeting 2", "John Doe");
        boolean deleted3 = repository.delete("Meeting 1", "Jane Smith");

        assertFalse(deleted1);
        assertFalse(deleted2);
        assertFalse(deleted3);
        assertEquals(3, repository.findAll(null, null, null, null, null, null, null).size());
    }

    @Test
    @Order(6)
    void testDeleteInvalidMeetingName() {
        boolean deleted1 = repository.delete("Meeting_1", "John Doe");

        assertFalse(deleted1);
        assertEquals(3, repository.findAll(null, null, null, null, null, null, null).size());
    }

    @Test
    @Order(7)
    void testAddParticipantNoPresentProvided() {
        List<String> participantsToAdd = new ArrayList<>();
        participantsToAdd.add("Participant 1");
        participantsToAdd.add("Participant 2");

        List<String> remaining = repository.addParticipant("Meeting 1", participantsToAdd);

        assertEquals(3, repository.findByName("Meeting 1").get().participants().size());
        assertTrue(remaining.isEmpty());
    }

    @Test
    @Order(8)
    void testAddParticipantNonExistingMeeting() {
        List<String> participantsToAdd = new ArrayList<>();
        participantsToAdd.add("Participant 2");
        participantsToAdd.add("Participant 3");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> repository.addParticipant("Meeting 5", participantsToAdd));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Meeting with such name was not found", exception.getReason());
    }

    @Test
    @Order(9)
    void testAddParticipantReturnsAlreadyAdded() {
        List<String> participantsToAdd = new ArrayList<>();
        participantsToAdd.add("Participant 1");
        participantsToAdd.add("Participant 2");
        participantsToAdd.add("John Doe");

        List<String> remaining = repository.addParticipant("Meeting 1", participantsToAdd);

        assertEquals(3, repository.findByName("Meeting 1").get().participants().size());
        assertFalse(remaining.isEmpty());
        assertTrue(remaining.contains("John Doe"));
    }

    @Test
    @Order(10)
    void testRemoveParticipantNonExistingMeeting() {
        List<String> participantsToRemove = new ArrayList<>();

        participantsToRemove.add("Participant 1");
        participantsToRemove.add("Participant 3");
        participantsToRemove.add("John Doe");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> repository.removeParticipant("Meeting 5", participantsToRemove));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Meeting with such name was not found", exception.getReason());
    }

    @Test
    @Order(11)
    void testRemoveParticipantResponsibleLeft() {
        List<String> participantsToAdd = new ArrayList<>();
        List<String> participantsToRemove = new ArrayList<>();

        participantsToAdd.add("Participant 1");
        participantsToAdd.add("Participant 2");
        participantsToAdd.add("John Doe");

        repository.addParticipant("Meeting 1", participantsToAdd);

        participantsToRemove.add("Participant 1");
        participantsToRemove.add("Participant 3");
        participantsToRemove.add("John Doe");

        repository.removeParticipant("Meeting 1", participantsToRemove);

        assertEquals(2, repository.findByName("Meeting 1").get().participants().size());
        assertTrue(repository.findByName("Meeting 1").get().participants().containsKey("John Doe"));
        assertTrue(repository.findByName("Meeting 1").get().participants().containsKey("Participant 2"));
        assertFalse(repository.findByName("Meeting 1").get().participants().containsKey("Participant 1"));
        assertFalse(repository.findByName("Meeting 1").get().participants().containsKey("Participant 3"));
    }
}
