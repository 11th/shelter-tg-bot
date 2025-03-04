package com.skypro.telegram_team.service;

import com.skypro.telegram_team.model.Animal;
import com.skypro.telegram_team.model.Shelter;
import com.skypro.telegram_team.model.User;
import com.skypro.telegram_team.repository.AnimalRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnimalServiceTest {
    @InjectMocks
    private AnimalService animalService;
    @Mock
    private AnimalRepository animalRepository;
    private Animal expectedAnimal;

    @BeforeEach
    public void setup() {
        expectedAnimal = new Animal();
        expectedAnimal.setId(1L);
        expectedAnimal.setName("sharik");
        expectedAnimal.setType(Animal.TypeAnimal.DOG);
        expectedAnimal.setState(Animal.AnimalStateEnum.IN_TEST);
        User user = new User();
        user.setId(1L);
        user.setState(User.OwnerStateEnum.PROBATION);
        expectedAnimal.setUser(user);
    }

    @Test
    public void createAnimal() {
        when(animalRepository.save(any())).thenReturn(expectedAnimal);
        Animal actualAnimal = animalService.create(expectedAnimal, expectedAnimal.getType());
        assertEquals(expectedAnimal, actualAnimal);
        verify(animalRepository, times(1)).save(any());
    }

    @Test
    public void findById() {
        when(animalRepository.findById(any())).thenReturn(Optional.ofNullable(expectedAnimal));
        Animal actualAnimal = animalService.findById(expectedAnimal.getId());
        assertEquals(expectedAnimal, actualAnimal);
        assertEquals(expectedAnimal.getId(), actualAnimal.getId());
        verify(animalRepository, times(1)).findById(any());
    }

    @Test
    public void deleteById() {
        when(animalRepository.findById(any())).thenReturn(Optional.ofNullable(expectedAnimal));
        Animal actualAnimal = animalService.deleteById(expectedAnimal.getId());
        assertEquals(expectedAnimal, actualAnimal);
        verify(animalRepository, times(1)).findById(any());
    }

    @Test
    public void updateAnimal() {
        Shelter shelter = new Shelter();
        Animal animalInDB = new Animal();
        animalInDB.setName("sharik");
        animalInDB.setId(1L);
        Animal updatedAnimal = new Animal();
        updatedAnimal.setName("pushok");
        updatedAnimal.setShelter(shelter);
        when(animalRepository.findById(any())).thenReturn(Optional.of(animalInDB));
        when(animalRepository.save(any())).thenReturn(updatedAnimal);
        Animal actualAnimal = animalService.update(updatedAnimal, animalInDB.getId());
        assertEquals(actualAnimal.getId(), animalInDB.getId());
        assertEquals(actualAnimal.getName(), updatedAnimal.getName());
        verify(animalRepository, times(1)).save(any());
        verify(animalRepository, times(1)).findById(any());
    }

    @Test
    public void findAll() {
        Animal animal1 = new Animal();
        animal1.setId(1L);
        animal1.setName("рекс");
        Animal animal2 = new Animal();
        animal2.setId(2L);
        animal2.setName("хатико");
        Animal animal3 = new Animal();
        animal3.setId(3L);
        animal3.setName("бетховен");
        when(animalRepository.findAll(Sort.by("name"))).thenReturn(List.of(animal1, animal2, animal3));
        List<Animal> allAnimals = animalService.findAll();
        assertTrue(allAnimals.size() != 0);
        verify(animalRepository, times(1)).findAll(Sort.by("name"));
    }

    @Test
    public void findByName() {
        List<Animal> expectedAnimals = List.of(expectedAnimal);
        when(animalRepository.findAnimalsByName(any())).thenReturn(Optional.of(expectedAnimals));
        List<Animal> actualAnimals = animalService.findByName(expectedAnimal.getName());
        assertEquals(expectedAnimals, actualAnimals);
        verify(animalRepository, times(1)).findAnimalsByName(any());
    }

    @Test
    public void findByUserId() {
        when(animalRepository.findAnimalsByUserId(any())).thenReturn(expectedAnimal);
        Animal actualAnimal = animalService.findByUserId(expectedAnimal.getUser().getId());
        assertEquals(expectedAnimal, actualAnimal);
        verify(animalRepository, times(1)).findAnimalsByUserId(any());
    }

    @Test
    public void findAllByUserIdNotNullAndState() {
        List<Animal> expectedAnimals = List.of(expectedAnimal);
        when(animalRepository.findAllByUserIdNotNullAndState(any())).thenReturn(expectedAnimals);
        List<Animal> actualAnimals = animalService.findAllByUserIdNotNullAndState(expectedAnimal.getState());
        assertEquals(expectedAnimals, actualAnimals);
        verify(animalRepository, times(1)).findAllByUserIdNotNullAndState(any());
    }

    @Test
    public void findByUserState() {
        List<Animal> expectedAnimals = List.of(expectedAnimal);
        when(animalRepository.findAnimalsByUserState(any())).thenReturn(expectedAnimals);
        List<Animal> actualAnimals = animalService.findByUserState(expectedAnimal.getUser().getState());
        assertEquals(expectedAnimals, actualAnimals);
        verify(animalRepository, times(1)).findAnimalsByUserState(any());
    }

    @Test
    public void ShouldThrowsIllegalStateExceptionWhenMethodUpdateRuns() {
        Animal animalInDB = new Animal();
        animalInDB.setId(1L);
        Shelter shelter = new Shelter();
        shelter.setType(Animal.TypeAnimal.DOG);
        Animal updatedAnimal = new Animal();
        updatedAnimal.setName("pushok");
        updatedAnimal.setShelter(shelter);
        updatedAnimal.setType(Animal.TypeAnimal.CAT);
        assertThrows(IllegalStateException.class, () -> animalService.update(updatedAnimal, animalInDB.getId()));
    }

    @Test
    public void photoUpload() throws Exception {
        //Given
        Resource resource = new ClassPathResource("photo/cat.jpeg");
        MockMultipartFile multipartFile = new MockMultipartFile("file", "file.jpeg", "", resource.getInputStream());
        Animal animal = new Animal();
        animal.setId(1L);
        animal.setPhoto(Files.readAllBytes(resource.getFile().toPath()));
        //When
        when(animalRepository.findById(any())).thenReturn(Optional.of(animal));
        when(animalRepository.save(animal)).thenReturn(animal);
        animalService.photoUpload(1L, multipartFile);
        //Then
        verify(animalRepository, times(1)).save(animal);
    }

    @Test
    public void photoDownload() throws Exception {
        //Given
        Resource resource = new ClassPathResource("photo/cat.jpeg");
        Animal expected = new Animal();
        expected.setId(1L);
        expected.setPhoto(Files.readAllBytes(resource.getFile().toPath()));
        //When
        when(animalRepository.findById(any())).thenReturn(Optional.of(expected));
        var actual = animalService.photoDownload(1L);
        //Then
        Assertions.assertThat(actual).isNotEmpty();
        Assertions.assertThat(Arrays.toString(actual)).isEqualTo(Arrays.toString(expected.getPhoto()));
    }
}
