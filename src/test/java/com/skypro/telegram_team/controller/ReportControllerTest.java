package com.skypro.telegram_team.controller;

import com.skypro.telegram_team.model.Animal;
import com.skypro.telegram_team.model.Report;
import com.skypro.telegram_team.model.User;
import com.skypro.telegram_team.repository.AnimalRepository;
import com.skypro.telegram_team.repository.ReportRepository;
import com.skypro.telegram_team.repository.ShelterRepository;
import com.skypro.telegram_team.repository.UserRepository;
import com.skypro.telegram_team.service.ReportService;
import com.skypro.telegram_team.service.ShelterService;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
public class ReportControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @InjectMocks
    private ReportController reportController;
    @MockBean
    private AnimalController animalController;
    @MockBean
    private UserController userController;
    @MockBean
    private ShelterController shelterController;
    @SpyBean
    private ReportService reportService;
    @SpyBean
    private ShelterService shelterService;
    @MockBean
    private ReportRepository reportRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private AnimalRepository animalRepository;
    @MockBean
    private ShelterRepository shelterRepository;
    private final Report report = new Report();
    private final JSONObject jsonReport = new JSONObject();
    private final JSONObject jsonAnimal = new JSONObject();
    private final JSONObject jsonUser = new JSONObject();
    private final User user = new User();
    private final Animal animal = new Animal();

    @BeforeEach
    public void setup() throws Exception {
        //user
        user.setState(User.OwnerStateEnum.PROBATION);
        user.setTelegramId(1L);
        user.setId(1L);
        //animal
        animal.setId(1L);
        animal.setName("sharik");
        animal.setState(Animal.AnimalStateEnum.IN_SHELTER);
        //report
        report.setId(1L);
        report.setDiet("diet");
        report.setChangeBehavior("behavior");
        report.setWellBeing("health");
        report.setDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        report.setUser(user);
        report.setAnimal(animal);
        //animaljson
        jsonAnimal.put("id", report.getAnimal().getId());
        jsonAnimal.put("name", report.getAnimal().getName());
        jsonAnimal.put("state", report.getAnimal().getState());
        //userjson
        jsonUser.put("id", report.getUser().getId());
        jsonUser.put("telegramId", report.getUser().getTelegramId());
        jsonUser.put("state", report.getUser().getState());
        //reportjson
        jsonReport.put("id", report.getId());
        jsonReport.put("diet", report.getDiet());
        jsonReport.put("changeBehavior", report.getChangeBehavior());
        jsonReport.put("wellBeing", report.getWellBeing());
        jsonReport.put("date", report.getDate());
        jsonReport.put("user", jsonUser);
        jsonReport.put("animal", jsonAnimal);
        //when
        when(reportRepository.findById(any())).thenReturn(Optional.of(report));
        when(reportRepository.findAll()).thenReturn(List.of(report));
        when(reportRepository.save(any())).thenReturn(report);
    }

    @Test
    public void findReportById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/reports/" + report.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diet").value(report.getDiet()))
                .andExpect(jsonPath("$.id").value(report.getId()))
                .andExpect(jsonPath("$.wellBeing").value(report.getWellBeing()))
                .andExpect(jsonPath("$.date").value(report.getDate().toString()))
                .andExpect(jsonPath("$.changeBehavior").value(report.getChangeBehavior()))
                .andExpect(jsonPath("$.user.id").value(report.getUser().getId()))
                .andExpect(jsonPath("$.user.telegramId").value(report.getUser().getTelegramId()))
                .andExpect(jsonPath("$.user.state").value(report.getUser().getState().toString()))
                .andExpect(jsonPath("$.animal.id").value(report.getAnimal().getId()))
                .andExpect(jsonPath("$.animal.name").value(report.getAnimal().getName()))
                .andExpect(jsonPath("$.animal.state").value(report.getAnimal().getState().toString()));
    }

    @Test
    public void deleteById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/reports/" + report.getId()))
                .andExpect(status().isOk());
    }

    @Test
    public void findAll() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].diet").value(report.getDiet()))
                .andExpect(jsonPath("$[0].id").value(report.getId()))
                .andExpect(jsonPath("$[0].wellBeing").value(report.getWellBeing()))
                .andExpect(jsonPath("$[0].date").value(report.getDate().toString()))
                .andExpect(jsonPath("$[0].changeBehavior").value(report.getChangeBehavior()))
                .andExpect(jsonPath("$[0].user.id").value(report.getUser().getId()))
                .andExpect(jsonPath("$[0].user.telegramId").value(report.getUser().getTelegramId()))
                .andExpect(jsonPath("$[0].user.state").value(report.getUser().getState().toString()))
                .andExpect(jsonPath("$[0].animal.id").value(report.getAnimal().getId()))
                .andExpect(jsonPath("$[0].animal.name").value(report.getAnimal().getName()))
                .andExpect(jsonPath("$[0].animal.state").value(report.getAnimal().getState().toString()));
    }

    @Test
    public void createReport() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/reports")
                        .content(jsonReport.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diet").value(report.getDiet()))
                .andExpect(jsonPath("$.id").value(report.getId()))
                .andExpect(jsonPath("$.wellBeing").value(report.getWellBeing()))
                .andExpect(jsonPath("$.date").value(report.getDate().toString()))
                .andExpect(jsonPath("$.changeBehavior").value(report.getChangeBehavior()))
                .andExpect(jsonPath("$.user.id").value(report.getUser().getId()))
                .andExpect(jsonPath("$.user.telegramId").value(report.getUser().getTelegramId()))
                .andExpect(jsonPath("$.user.state").value(report.getUser().getState().toString()))
                .andExpect(jsonPath("$.animal.id").value(report.getAnimal().getId()))
                .andExpect(jsonPath("$.animal.name").value(report.getAnimal().getName()))
                .andExpect(jsonPath("$.animal.state").value(report.getAnimal().getState().toString()));
    }

    @Test
    public void updateReport() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/reports/" + report.getId())
                        .content(jsonReport.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diet").value(report.getDiet()))
                .andExpect(jsonPath("$.id").value(report.getId()))
                .andExpect(jsonPath("$.wellBeing").value(report.getWellBeing()))
                .andExpect(jsonPath("$.date").value(report.getDate().toString()))
                .andExpect(jsonPath("$.changeBehavior").value(report.getChangeBehavior()))
                .andExpect(jsonPath("$.user.id").value(report.getUser().getId()))
                .andExpect(jsonPath("$.user.telegramId").value(report.getUser().getTelegramId()))
                .andExpect(jsonPath("$.user.state").value(report.getUser().getState().toString()))
                .andExpect(jsonPath("$.animal.id").value(report.getAnimal().getId()))
                .andExpect(jsonPath("$.animal.name").value(report.getAnimal().getName()))
                .andExpect(jsonPath("$.animal.state").value(report.getAnimal().getState().toString()));
    }

    @Test
    public void photoDownload() throws Exception {
        //Given
        Resource resource = new ClassPathResource("photo/cat.jpeg");
        byte[] photo = Files.readAllBytes(resource.getFile().toPath());
        Report expected = new Report();
        expected.setId(1L);
        expected.setPhoto(photo);
        //When
        when(reportRepository.findById(1L)).thenReturn(Optional.of(expected));
        //Then
        mockMvc.perform(MockMvcRequestBuilders.get("/reports/1/photo"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(expected.getPhoto()));
    }
}
