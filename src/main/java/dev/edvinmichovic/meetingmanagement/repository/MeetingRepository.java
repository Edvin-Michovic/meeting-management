package dev.edvinmichovic.meetingmanagement.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.edvinmichovic.meetingmanagement.dto.MeetingDTO;
import dev.edvinmichovic.meetingmanagement.model.Category;
import dev.edvinmichovic.meetingmanagement.model.Meeting;
import dev.edvinmichovic.meetingmanagement.model.Type;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class MeetingRepository {

    private final List<Meeting> meetingList = new ArrayList<>();
    private final String jsonFilePathFromContentRoot;
    private final String jsonFilePathFromSourceRoot;

    public MeetingRepository() {
        this.jsonFilePathFromContentRoot = "src/main/resources/json/meetings.json";
        this.jsonFilePathFromSourceRoot = "/json/meetings.json";
    }

    /**
     * Method allows to READ the meetings.
     * Depending on the parameter that was sent, filter to the result list is assigned.
     * E.g. it will filter meetings by description, AND e.g. type.
     * @param description - parameter allows to filter by description.
     *                    if the description is "Jono Java meetas", searching for
     *                    "java" returns the entry.
     * @param responsiblePerson - parameter allows to filter by responsible person.
     * @param category - parameter allows to filter by category.
     * @param type - parameter allows to filter by type.
     * @param startDate - parameter allows to filter by starting date.
     * @param endDate - parameter allows to filter by ending date.
     *                Meetings can be filtered between the dates.
     * @param minAttendees - parameter allows to filter number of attendees.
     *                     Note: if the minAttendees is 10, it will show the
     *                     meetings that have 10 or more attendees.
     * @return - method returns the list of meetings found.
     */
    public List<Meeting> findAll(String description,
                                 String responsiblePerson,
                                 String category,
                                 String type,
                                 LocalDate startDate,
                                 LocalDate endDate,
                                 Integer minAttendees) {
        return meetingList.stream()
                .filter(meeting -> description == null || meeting.description().toLowerCase().contains(description.toLowerCase()))
                .filter(meeting -> responsiblePerson == null || meeting.responsiblePerson().equals(responsiblePerson))
                .filter(meeting -> category == null || meeting.meetingCategory().equals(Category.valueOf(category)))
                .filter(meeting -> type == null || meeting.meetingType().equals(Type.valueOf(type)))
                .filter(meeting -> startDate == null || meeting.startDate().isAfter(startDate.atStartOfDay()))
                .filter(meeting -> endDate == null || meeting.endDate().isBefore(endDate.atTime(23,59)))
                .filter(meeting -> minAttendees == null || meeting.participants().size() >= minAttendees)
                .collect(Collectors.toList());
    }

    /**
     * Method implemented to FIND the existing meeting by its name.
     * @param name - the name of the meeting, represented in the String format.
     * @return - returns the optional list of meetings.
     */
    public Optional<Meeting> findByName(String name) {
        return meetingList.stream()
                .filter(meeting-> meeting.name().equals(name))
                .findFirst();
    }


    /**
     * Method to SAVE/ADD new meeting(s) into the meetings list.
     * The method will necessarily add the responsible person into the participants list, even
     * if the responsible person is already added.
     * Worth to mention that meeting with the same naming will be overwritten.
     * @param meetingDTO - the DTO representation of meeting class.
     *                   The data of the meetingDTO object is converted
     *                   to Meeting class object.
     */
    public void save(MeetingDTO meetingDTO) {
        meetingList.removeIf(meeting -> meeting.name().equals(meetingDTO.name()));
        HashMap<String, LocalDateTime> participants = new HashMap<>();

        if (meetingDTO.participants() != null) {
            participants = meetingDTO.participants();
        }

        participants.put(meetingDTO.responsiblePerson(), LocalDateTime.now().withSecond(0).withNano(0));

        Meeting meeting = new Meeting(meetingDTO.name(),
                meetingDTO.responsiblePerson(),
                meetingDTO.description(),
                Category.valueOf(meetingDTO.meetingCategory()),
                Type.valueOf(meetingDTO.meetingType()),
                meetingDTO.startDate(),
                meetingDTO.endDate(),
                participants);
        meetingList.add(meeting);
    }

    /**
     * Method implemented to DELETE the meeting.
     * It double-checks the naming, and the responsible person provided.
     * In case of the meeting, with correct naming, and correct responsible person provided exists, the meeting will be successfully deleted.
     * @param name - name of the meeting, that is going to be deleted.
     * @param responsiblePerson - name of the responsible person for particular meeting.
     */
    public boolean delete(String name, String responsiblePerson) {
        return meetingList.removeIf(m -> m.name().equals(name) && m.responsiblePerson().equals(responsiblePerson));
    }

    /**
     * Method implemented to ADD PARTICIPANTS to the meeting.
     * It only adds the participants, that were not present in the meeting.
     * In case of the duplicates of attendees (that are present in the meeting already),
     * it will return the list of the participants, that were not added for that reason.
     * @param name - the name of the meeting.
     * @param participants - list of the participant(s) names.
     * @return - returns the list of the duplicates attendees (the ones, that were not added,
     *         because they are already present)
     */
    public List<String> addParticipant(String name, List<String> participants) {
        Meeting meeting = findByName(name)
                .orElseThrow(() -> { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting with such name was not found"); });
        Iterator<String> iterator = participants.iterator();

        while (iterator.hasNext()) {
            String participant = iterator.next();

            if (!meeting.participants().containsKey(participant)) {
                meeting.participants().put(participant, LocalDateTime.now().withSecond(0).withNano(0));
                iterator.remove();
            }
        }

        return participants;
    }

    /**
     * Method implemented to REMOVE PARTICIPANTS from the meeting.
     * It only removes the participants, that are present in the meeting.
     * Also, the method does not delete the responsible person.
     * @param name - the name of the meeting.
     * @param participants - list of the participant(s) names.
     */
    public void removeParticipant(String name, List<String> participants) {
        Meeting meeting = findByName(name)
                .orElseThrow(() -> { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting with such name was not found"); });

        Stream<String> participantsToBeRemoved = participants.stream().filter(p -> meeting.participants().containsKey(p) && !meeting.responsiblePerson().equals(p));
        participantsToBeRemoved.forEach(p -> meeting.participants().remove(p));
    }

    /**
     * PostConstruct that loads the data from the meetings.json file as the program starts.
     * The root is Source Root.
     */
    @PostConstruct
    private void init() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        TypeReference<List<MeetingDTO>> typeReference = new TypeReference<>() {};

        try (InputStream inputStream = TypeReference.class.getResourceAsStream(jsonFilePathFromSourceRoot)) {
            List<MeetingDTO> meetingDTOList = mapper.readValue(inputStream, typeReference);
            meetingDTOList.forEach(this::save);
            System.out.println("Meetings successfully detected in .json file.");
        } catch (IOException e) {
            System.out.println("Unable to read any meetings: " + e.getMessage());
        }
    }

    /**
     * PreDestroy that saves the data from the meetings list to the meetings.json file as the program finishes.
     * The root is Content Root.
     */
    @PreDestroy
    private void preDestroy() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        try (OutputStream outputStream = new FileOutputStream(jsonFilePathFromContentRoot)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(outputStream,
                    this.findAll(null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null));
            System.out.println("Successfully saved all information into .json file.");
        } catch (IOException e) {
            System.out.println("Unable to save meetings' information: " + e.getMessage());
        }

    }

}
