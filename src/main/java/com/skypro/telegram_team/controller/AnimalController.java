package com.skypro.telegram_team.controller;

import com.skypro.telegram_team.model.Animal;
import com.skypro.telegram_team.service.AnimalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/animals")
public class AnimalController {
    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @Operation(summary = "поиск животного в БД по личному идентификатору", tags = "Animals"
            , responses = {@ApiResponse(
            responseCode = "200",
            description = "Найденное животное",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Animal.class)
            )
    ), @ApiResponse(responseCode = "404",
            description = "Животное не найдено",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(example = """
                            {
                              "timestamp": "2023-04-02T16:20:14.430+00:00",
                              "status": 404,
                              "error": "Bad Request",
                              "path": "/animals/{id}"
                            }""")
            )
    ), @ApiResponse(
            responseCode = "400",
            description = "Неверный запрос",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(example = """
                            {
                              "timestamp": "2023-04-02T16:20:14.430+00:00",
                              "status": 400,
                              "error": "Bad Request",
                              "path": "/animals/{id}"
                            }""")
            )
    ), @ApiResponse(
            responseCode = "500",
            description = "Проблемы на стороне сервера",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(example = """
                            {
                              "timestamp": "2023-04-02T16:22:05.642+00:00",
                              "status": 500,
                              "error": "Internal Server Error",
                              "path": "/animals/{id}"
                            }""")
            )
    )
    })
    @GetMapping("/{id}")
    public Animal findById(@Parameter(description = "Идентификатор животного") @PathVariable long id) {
        return animalService.findById(id);
    }

    @Operation(summary = "поиск животного в БД по имени", tags = "Animals")
    @GetMapping("/name")
    public List<Animal> findByName(@RequestParam String name) {
        return animalService.findByName(name);
    } //

    @Operation(summary = "Получение списка всех животных из БД"
            , tags = "Animals"
            , responses = {@ApiResponse(
            responseCode = "200",
            description = "Список всех животных из БД",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = Animal.class))
            )
    ), @ApiResponse(
            responseCode = "400",
            description = "Неверный запрос",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(example = """
                            {
                              "timestamp": "2023-04-02T16:20:14.430+00:00",
                              "status": 400,
                              "error": "Bad Request",
                              "path": "/animals/"
                            }""")
            )
    ), @ApiResponse(
            responseCode = "500",
            description = "Проблемы на стороне сервера",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(example = """
                            {
                              "timestamp": "2023-04-02T16:20:14.430+00:00",
                              "status": 500,
                              "error": "Internal Server Error",
                              "path": "/animals/"
                            }""")
            )
    )
    })
    @GetMapping
    public Iterable<Animal> getAllAnimals() {
        return animalService.findAll();
    }

    @Operation(summary = "Удаление животного из БД по личному идентификатору"
            , tags = "Animals"
            , responses = {@ApiResponse(
            responseCode = "200",
            description = "Удаленное из БД животное",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Animal.class)
            )
    ), @ApiResponse(responseCode = "404",
            description = "Животное по указанному идентификатору не найдено",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE
                    , schema = @Schema(example = """
                    {
                      "timestamp": "2023-04-02T16:20:14.430+00:00",
                      "status": 404,
                      "error": "Bad Request",
                      "path": "/animals/{id}"
                    }"""))
    ), @ApiResponse(
            responseCode = "400",
            description = "Неверный запрос",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(example = """
                            {
                              "timestamp": "2023-04-02T16:20:14.430+00:00",
                              "status": 400,
                              "error": "Bad Request",
                              "path": "/animals/{id}"
                            }""")
            )
    ), @ApiResponse(
            responseCode = "500",
            description = "Проблемы на стороне сервера",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(example = """
                            {
                              "timestamp": "2023-04-02T16:20:14.430+00:00",
                              "status": 500,
                              "error": "Internal Server Error",
                              "path": "/animals/{id}"
                            }""")
            )
    )
    })
    @DeleteMapping("/{id}")
    public Animal deleteAnimal(@Parameter(description = "Идентификатор животного", example = "1") @PathVariable long id) {
        return animalService.deleteById(id);
    }

    @Operation(
            summary = "Изменение данных о животном по личному идентификатору"
            , tags = "Animals",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Ввод новых данных о животном",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Animal.class)
                    )
            ),
            responses = {@ApiResponse(responseCode = "200"
                    , description = "Животное с измененными данными",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Animal.class))
            ), @ApiResponse(responseCode = "404"
                    , description = "Животное по данному идентификатору не найдено",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE
                            , schema = @Schema(example = """
                            {
                              "timestamp": "2023-04-02T16:20:14.430+00:00",
                              "status": 404,
                              "error": "Bad Request",
                              "path": "/animals/{id}"
                            }"""))
            ), @ApiResponse(
                    responseCode = "400",
                    description = "Неверный запрос",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = """
                                    {
                                      "timestamp": "2023-04-02T16:20:14.430+00:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "path": "/animals/{id}"
                                    }""")
                    )
            ), @ApiResponse(
                    responseCode = "500",
                    description = "Проблемы на стороне сервера",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = """
                                    {
                                      "timestamp": "2023-04-02T16:20:14.430+00:00",
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "path": "/animals/{id}"
                                    }""")
                    )
            )}
    )
    @PutMapping("/{id}")
    public Animal updateAnimal(@RequestBody Animal animal,
                               @Parameter(description = "Личный идентификатор животного.", example = "1") @PathVariable Long id) {
        return animalService.update(animal, id);
    }

    @Operation(summary = "Создание животного.", tags = "Animals")
    @PostMapping
    public Animal createAnimal(@RequestBody Animal animal, @RequestParam("type") Animal.TypeAnimal type) {
        return animalService.create(animal, type);
    }

    @Operation(summary = "Загрузка фото животного", tags = "Animals")
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> photoUpload(@PathVariable("id") Long id,
                                              @RequestParam("photo") MultipartFile file) throws IOException {
        animalService.photoUpload(id, file);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Выгрузка фото животного", tags = "Animals")
    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> photoDownload(@PathVariable("id") Long id) {
        var photo = animalService.photoDownload(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(photo.length);
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(photo);
    }
}
