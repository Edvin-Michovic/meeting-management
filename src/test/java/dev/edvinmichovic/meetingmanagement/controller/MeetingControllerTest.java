package dev.edvinmichovic.meetingmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.edvinmichovic.meetingmanagement.dto.MeetingDTO;
import dev.edvinmichovic.meetingmanagement.model.Category;
import dev.edvinmichovic.meetingmanagement.model.Meeting;
import dev.edvinmichovic.meetingmanagement.model.Type;
import dev.edvinmichovic.meetingmanagement.repository.MeetingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MeetingControllerTest {

    @MockBean
    private MeetingRepository repository;
    @Autowired
    private MockMvc mockMvc;

    private List<Meeting> meetings;

    @BeforeEach
    void setUp() {
        String[] names = {
                "Gabriel Rogers",
                "Nicholas Chambers",
                "Sebastian Andrews",
                "Dylan Barnes",
        };
        HashMap<String, LocalDateTime> participants = new HashMap<>();

        for (String name : names) {
            participants.put(name, LocalDateTime.now().withSecond(0).withNano(0));
        }

        Meeting meeting1 = new Meeting("Meeting 1",
                names[0],
                "My first Java meeting ever.",
                Category.Hub,
                Type.Live,
                LocalDateTime.parse("2020-05-23T10:00"),
                LocalDateTime.parse("2020-05-23T12:00"),
                participants);
        Meeting meeting2 = new Meeting("Meeting 2",
                names[1],
                "Definitely not a Java meeting.",
                Category.TeamBuilding,
                Type.InPerson,
                LocalDateTime.parse("2020-05-30T15:00"),
                LocalDateTime.parse("2020-05-30T16:00"),
                null);

        meetings = Arrays.asList(meeting1, meeting2);
    }

    @Test
    @Order(1)
    void testFindAllNoFilters() throws Exception {
        when(repository.findAll(any(), any(), any(), any(), any(),any(), any())).thenReturn(meetings);

        MvcResult result = mockMvc.perform(get("/meetings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Meeting 1"))
                .andExpect(jsonPath("$[0].responsiblePerson").value("Gabriel Rogers"))
                .andExpect(jsonPath("$[0].description").value("My first Java meeting ever."))
                .andExpect(jsonPath("$[0].meetingCategory").value("Hub"))
                .andExpect(jsonPath("$[0].meetingType").value("Live"))
                .andExpect(jsonPath("$[0].startDate").exists())
                .andExpect(jsonPath("$[0].endDate").exists())
                .andExpect(jsonPath("$[0].participants").exists())
                .andExpect(jsonPath("$[1].name").value("Meeting 2"))
                .andExpect(jsonPath("$[1].responsiblePerson").value("Nicholas Chambers"))
                .andExpect(jsonPath("$[1].description").value("Definitely not a Java meeting."))
                .andExpect(jsonPath("$[1].meetingCategory").value("TeamBuilding"))
                .andExpect(jsonPath("$[1].meetingType").value("InPerson"))
                .andExpect(jsonPath("$[1].startDate").exists())
                .andExpect(jsonPath("$[1].endDate").exists())
                .andExpect(jsonPath("$[1].participants").doesNotExist())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        System.out.println(response);
    }

    @Test
    @Order(2)
    void testFindAllDescriptionFilter() throws Exception {
        when(repository.findAll(eq("dEfInItElY"), any(), any(), any(), any(), any(), any())).thenReturn(Collections.singletonList(meetings.get(1)));

        MvcResult result = mockMvc.perform(get("/meetings")
                        .param("description", "dEfInItElY")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].description").value("Definitely not a Java meeting."))
                .andExpect(jsonPath("$[1]").doesNotExist())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        System.out.println(response);
    }

    @Test
    @Order(3)
    void testFindAllResponsibleFilter() throws Exception {
        when(repository.findAll(any(), eq("Gabriel Rogers"), any(), any(), any(),any(), any())).thenReturn(Collections.singletonList(meetings.get(0)));

        MvcResult result = mockMvc.perform(get("/meetings")
                        .param("responsiblePerson", "Gabriel Rogers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].responsiblePerson").value("Gabriel Rogers"))
                .andExpect(jsonPath("$[1]").doesNotExist())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        System.out.println(response);
    }

    @Test
    @Order(4)
    void testFindAllCategoryFilter() throws Exception {
        when(repository.findAll(any(), any(), eq("Hub"), any(), any(),any(), any())).thenReturn(Collections.singletonList(meetings.get(0)));

        MvcResult result = mockMvc.perform(get("/meetings")
                        .param("category", "Hub")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].meetingCategory").value("Hub"))
                .andExpect(jsonPath("$[1]").doesNotExist())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        System.out.println(response);
    }

    @Test
    @Order(5)
    void testFindAllTypeFilter() throws Exception {
        when(repository.findAll(any(), any(), any(), eq("InPerson"), any(),any(), any())).thenReturn(Collections.singletonList(meetings.get(1)));

        MvcResult result = mockMvc.perform(get("/meetings")
                        .param("type", "InPerson")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].meetingType").value("InPerson"))
                .andExpect(jsonPath("$[1]").doesNotExist())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        System.out.println(response);
    }

    @Test
    @Order(6)
    void testFindAllStartDateFilter() throws Exception {
        when(repository.findAll(any(), any(), any(), any(), eq(LocalDate.parse("2020-05-30")),any(), any())).thenReturn(Collections.singletonList(meetings.get(1)));

        MvcResult result = mockMvc.perform(get("/meetings")
                        .param("startDate", "2020-05-30")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].startDate").value("2020-05-30T15:00:00"))
                .andExpect(jsonPath("$[0].endDate").value("2020-05-30T16:00:00"))
                .andExpect(jsonPath("$[1]").doesNotExist())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        System.out.println(response);
    }

    @Test
    @Order(7)
    void testFindAllBetweenDatesFilter() throws Exception {
        when(repository.findAll(any(), any(), any(), any(), eq(LocalDate.parse("2020-05-20")), eq(LocalDate.parse("2020-05-23")), any())).thenReturn(Collections.singletonList(meetings.get(0)));

        MvcResult result = mockMvc.perform(get("/meetings")
                        .param("startDate", "2020-05-20")
                        .param("endDate", "2020-05-23")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].startDate").value("2020-05-23T10:00:00"))
                .andExpect(jsonPath("$[0].endDate").value("2020-05-23T12:00:00"))
                .andExpect(jsonPath("$[1]").doesNotExist())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        System.out.println(response);
    }

    @Test
    @Order(8)
    void testFindAllMinAttendeesResultFilter() throws Exception {
        when(repository.findAll(any(), any(), any(), any(), any(), any(), eq(1))).thenReturn(Collections.singletonList(meetings.get(0)));

        MvcResult result = mockMvc.perform(get("/meetings")
                        .param("minAttendees", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].participants").exists())
                .andExpect(jsonPath("$[1]").doesNotExist())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        System.out.println(response);
    }

    @Test
    @Order(9)
    void testFindAllMinAttendeesNoResultFilter() throws Exception {
        when(repository.findAll(any(), any(), any(), any(), any(), any(), eq(5))).thenReturn(null);

        MvcResult result = mockMvc.perform(get("/meetings")
                        .param("minAttendees", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").doesNotExist())
                .andExpect(jsonPath("$[1]").doesNotExist())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        System.out.println(response);
    }

    @Test
    @Order(10)
    void testFindByNameFound() throws Exception {
        when(repository.findByName("Meeting 1")).thenReturn(Optional.ofNullable(meetings.get(0)));

        MvcResult result = mockMvc.perform(get("/meetings/{name}", "Meeting 1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.name").value("Meeting 1"))
                .andReturn();

        verify(repository, times(1)).findByName("Meeting 1");

        String response = result.getResponse().getContentAsString();
        System.out.println(response);
    }

    @Test
    @Order(11)
    void testFindByNameNotFound() throws Exception {
        when(repository.findByName("Meeting 3")).thenReturn(Optional.empty());

        MvcResult result = mockMvc.perform(get("/meetings/{name}", "Meeting 3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Meeting with that name was not found."))
                .andReturn();

        verify(repository, times(1)).findByName("Meeting 3");

        String response = result.getResponse().getContentAsString();
        System.out.println(response);
    }

    @Test
    @Order(12)
    void testCreateNewMeeting() throws Exception {
        MeetingDTO meetingDTO = new MeetingDTO("Meeting 3",
                "Gabriel Saguaro",
                "Main meeting.",
                "CodeMonkey",
                "Live",
                LocalDateTime.now().plusMinutes(1).withSecond(0).withNano(0),
                LocalDateTime.now().plusMinutes(2).withSecond(0).withNano(0),
                null);


        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        MvcResult result = mockMvc.perform(post("/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(meetingDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        verify(repository, times(1)).save(eq(meetingDTO));

        String response = result.getResponse().getContentAsString();
        System.out.println(response);
    }

    @Test
    @Order(13)
    void testRemoveMeetingValid () throws Exception {
        when(repository.delete("Meeting 1", "Gabriel Rogers")).thenReturn(true);

        mockMvc.perform(delete("/meetings/{name}", "Meeting 1")
                .param("responsiblePerson", "Gabriel Rogers"))
                .andExpect(status().isNoContent());

        verify(repository, times(1)).delete("Meeting 1", "Gabriel Rogers");
    }

    @Test
    @Order(14)
    void testRemoveMeetingInvalid () throws Exception {
        when(repository.delete("Meeting 1", "Gabriel Rogers")).thenReturn(false);

        mockMvc.perform(delete("/meetings/{name}", "Meeting 1")
                        .param("responsiblePerson", "Gabriel Rogers"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Only responsible person can delete the meeting.\n" +
                        "Please make sure the responsible person is correct for this meeting."));

        verify(repository, times(1)).delete("Meeting 1", "Gabriel Rogers");
    }

    @Test
    @Order(15)
    void testAddParticipantsNoParticipantsProvided () throws Exception {
        List<String> participants = Collections.emptyList();
        String jsonRequest = new ObjectMapper().writeValueAsString(participants);

        mockMvc.perform(put("/meetings/{name}/addParticipant", "Meeting 1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Participants list is empty."));
    }

    @Test
    @Order(16)
    void testAddParticipantsHasAlreadyInvited () throws Exception {
        List<String> participants = Arrays.asList("Bill", "Gabe", "John");
        String jsonRequest = new ObjectMapper().writeValueAsString(participants);
        when(repository.addParticipant("Meeting 1", participants)).thenReturn(participants);

        mockMvc.perform(put("/meetings/{name}/addParticipant", "Meeting 1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value("""
                        WARNING! [Bill, Gabe, John] participant are already invited.
                        Already invited participant(s) won't be added to the meeting's participants list.
                        All other users that were not present in the meeting before will be added."""));
    }

    @Test
    @Order(17)
    void testAddParticipantsValid () throws Exception {
        List<String> participants = Arrays.asList("Bill", "Gabe", "John");
        String jsonRequest = new ObjectMapper().writeValueAsString(participants);
        when(repository.addParticipant("Meeting 1", participants)).thenReturn(Collections.emptyList());

        mockMvc.perform(put("/meetings/{name}/addParticipant", "Meeting 1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value("Successfully added participants."));
    }

    @Test
    @Order(18)
    void testRemoveParticipantsNoParticipantsProvided () throws Exception {
        List<String> participants = Collections.emptyList();
        String jsonRequest = new ObjectMapper().writeValueAsString(participants);

        mockMvc.perform(delete("/meetings/{name}/removeParticipant", "Meeting 1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Participants list is empty."));
    }

    @Test
    @Order(19)
    void testRemoveParticipantsValid() throws Exception {
        List<String> participants = Arrays.asList("Bill", "Gabe", "John");
        String jsonRequest = new ObjectMapper().writeValueAsString(participants);

        mockMvc.perform(delete("/meetings/{name}/removeParticipant", "Meeting 1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Participants, if they were present, are successfully deleted.\n" +
                        "NOTE: Meeting's responsible person won't be deleted from the meeting."));
    }

}
