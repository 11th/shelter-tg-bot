package com.skypro.telegram_team.handler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import com.skypro.telegram_team.exception.InvalidDataException;
import com.skypro.telegram_team.handler.buffer.Question;
import com.skypro.telegram_team.handler.buffer.QuestionsBuffer;
import com.skypro.telegram_team.handler.buffer.Request;
import com.skypro.telegram_team.handler.buffer.RequestsBuffer;
import com.skypro.telegram_team.keyboard.Callback;
import com.skypro.telegram_team.keyboard.InlineKeyboard;
import com.skypro.telegram_team.keyboard.Menu;
import com.skypro.telegram_team.keyboard.MenuKeyboard;
import com.skypro.telegram_team.listener.BotListenerUtil;
import com.skypro.telegram_team.model.Report;
import com.skypro.telegram_team.model.User;
import com.skypro.telegram_team.service.ReportService;
import com.skypro.telegram_team.service.ShelterService;
import com.skypro.telegram_team.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BotMessageHandlerTest {
    @Mock
    private TelegramBot telegramBot;
    @Mock
    private QuestionsBuffer questionsBuffer;
    @Mock
    private RequestsBuffer requestsBuffer;
    @Mock
    private ShelterService shelterService;
    @Mock
    private UserService userService;
    @Mock
    private ReportService reportService;

    @InjectMocks
    private BotMessageHandler out;

    @BeforeEach
    void setUp() {
        out = new BotMessageHandler(telegramBot, questionsBuffer, requestsBuffer, shelterService, userService, reportService);
        when(userService.findByTelegramId(any())).thenReturn(BotListenerUtil.mockUser());
    }

    @ParameterizedTest
    @MethodSource("provideParamsForMenuTests")
    void processMessage_MenuItems(String menuText, String message) throws Exception {
        //Given
        Update update = BotListenerUtil.generateUpdate(menuText);
        //When
        var actual = out.processMessage(update.message());
        //Then
        Assertions.assertThat(actual.isEmpty()).isFalse();
        Assertions.assertThat(actual.get(0).getParameters().get("chat_id")).isEqualTo(update.message().chat().id());
        Assertions.assertThat(actual.get(0).getParameters().get("text")).isEqualTo(message);
        Assertions.assertThat(actual.get(0).getParameters().get("reply_markup")).isNotNull();
    }

    static Stream<Arguments> provideParamsForMenuTests() {
        return Stream.of(
                Arguments.of(Menu.START.getText(), "Привет! Для продолжения работы выберите приют."),
                Arguments.of(Menu.SET_SHELTER.getText(), "Выберите приют"),
                Arguments.of(Menu.GET_INFO.getText(), "Информация о приюте"),
                Arguments.of(Menu.GET_ANIMAL.getText(), "Как взять животное"),
                Arguments.of(Menu.SEND_REPORT.getText(), "Какие данные отправить?"),
                Arguments.of(Menu.SET_USER_DATA.getText(), "Какие данные записать?"),
                Arguments.of(Menu.ASK_VOLUNTEER.getText(), "Кого спросить?")
        );
    }

    @ParameterizedTest
    @MethodSource("provideKeyboardsForMenuTests")
    void processMessage_Keyboards(String menuText, Keyboard expectedKeyboard) throws IOException {
        //Given
        Update update = BotListenerUtil.generateUpdate(menuText);
        //When
        var actual = out.processMessage(update.message());
        var actualKeyboard = (Keyboard) actual.get(0).getParameters().get("reply_markup");
        //Then
        Assertions.assertThat(actualKeyboard).isNotNull();
        Assertions.assertThat(actualKeyboard).isEqualTo(expectedKeyboard);
    }

    static Stream<Arguments> provideKeyboardsForMenuTests() {
        return Stream.of(
                Arguments.of(Menu.START.getText(), MenuKeyboard.START_KEYBOARD.getKeyboard()),
                Arguments.of(Menu.GET_INFO.getText(), InlineKeyboard.SHELTER_INFO.getMarkup()),
                Arguments.of(Menu.GET_ANIMAL.getText(), InlineKeyboard.ANIMAL_INFO.getMarkup()),
                Arguments.of(Menu.SEND_REPORT.getText(), InlineKeyboard.REPORT_DATA.getMarkup()),
                Arguments.of(Menu.SET_USER_DATA.getText(), InlineKeyboard.USER_DATA.getMarkup())
        );
    }

    @Test
    void processMessage_KeyboardShelter() throws IOException {
        //Given
        Update update = BotListenerUtil.generateUpdate(Menu.SET_SHELTER.getText());
        //When
        when(shelterService.findAll()).thenReturn(Collections.singletonList(BotListenerUtil.mockShelter()));
        var actual = out.processMessage(update.message());
        InlineKeyboardButton[][] buttons = ((InlineKeyboardMarkup) actual.get(0).getParameters().get("reply_markup")).inlineKeyboard();
        //Then
        Assertions.assertThat(buttons[0][0].callbackData()).isEqualTo("SAVE_SHELTER1");
        Assertions.assertThat(buttons[0][0].text()).isEqualTo("Dogs");
    }

    @Test
    void processMessage_KeyboardVolunteer() throws IOException {
        //Given
        Update update = BotListenerUtil.generateUpdate(Menu.ASK_VOLUNTEER.getText());
        //When
        when(userService.findVolunteers()).thenReturn(Collections.singletonList(BotListenerUtil.mockVolunteer()));
        var actual = out.processMessage(update.message());
        InlineKeyboardButton[][] buttons = ((InlineKeyboardMarkup) actual.get(0).getParameters().get("reply_markup")).inlineKeyboard();
        //Then
        Assertions.assertThat(buttons[0][0].callbackData()).isEqualTo("ASK_VOLUNTEER12");
        Assertions.assertThat(buttons[0][0].text()).isEqualTo("name");
        Assertions.assertThat(buttons[1][0].callbackData()).isEqualTo(Callback.ASK_ANY_VOLUNTEER.name());
        Assertions.assertThat(buttons[1][0].text()).isEqualTo(Callback.ASK_ANY_VOLUNTEER.getText());
    }

    @Test
    void processMessage_UserSavePhone() throws Exception {
        //Given
        Update update = BotListenerUtil.generateUpdate("+79511338877");
        Request request = new Request(update.message().chat().id());
        request.setUserPhoneRequested(true);
        //When
        when(requestsBuffer.getRequest(any())).thenReturn(Optional.of(request));
        when(userService.update(any(), any())).thenReturn(BotListenerUtil.mockUser());
        var actual = out.processMessage(update.message());
        //Then
        Assertions.assertThat(actual.isEmpty()).isFalse();
        Assertions.assertThat(actual.get(0).getParameters().get("chat_id")).isEqualTo(update.message().chat().id());
        Assertions.assertThat(actual.get(0).getParameters().get("text")).isEqualTo("Данные пользователя записаны");
    }

    @Test
    void processMessage_UserSaveWithException() throws Exception {
        //Given
        Update update = BotListenerUtil.generateUpdate("11@ru");
        Request request = new Request(update.message().chat().id());
        request.setUserEmailRequested(true);
        //When
        when(requestsBuffer.getRequest(any())).thenReturn(Optional.of(request));
        when(userService.update(any(), any())).thenThrow(new InvalidDataException("error"));
        var actual = out.processMessage(update.message());
        //Then
        Assertions.assertThat(actual.isEmpty()).isFalse();
        Assertions.assertThat(actual.get(0).getParameters().get("chat_id")).isEqualTo(update.message().chat().id());
        Assertions.assertThat(actual.get(0).getParameters().get("text")).isEqualTo("Возникла ошибка: error");
    }

    @Test
    void processMessage_ReportSaveDiet() throws Exception {
        //Given
        Update update = BotListenerUtil.generateUpdate("ok");
        Request request = new Request(update.message().chat().id());
        request.setReportDietRequested(true);
        Report report = new Report();
        report.setId(1L);
        //When
        when(requestsBuffer.getRequest(any())).thenReturn(Optional.of(request));
        when(reportService.findFirstByUserIdAndDate(any(), any())).thenReturn(report);
        when(reportService.update(any(), any())).thenReturn(report);
        var actual = out.processMessage(update.message());
        //Then
        Assertions.assertThat(actual.isEmpty()).isFalse();
        Assertions.assertThat(actual.get(0).getParameters().get("chat_id")).isEqualTo(update.message().chat().id());
        Assertions.assertThat(actual.get(0).getParameters().get("text")).isEqualTo("Данные отчета записаны");
    }

    @Test
    void processMessage_ReportSavePhoto() throws Exception {
        //Given
        Update update = BotListenerUtil.generateUpdateWithPhoto();
        Request request = new Request(update.message().chat().id());
        request.setReportPhotoRequested(true);
        Report report = new Report();
        report.setId(1L);
        //When
        when(telegramBot.execute(any())).thenReturn(BotListenerUtil.generateResponseOk());
        when(requestsBuffer.getRequest(any())).thenReturn(Optional.of(request));
        when(reportService.findFirstByUserIdAndDate(any(), any())).thenReturn(report);
        when(reportService.update(any(), any())).thenReturn(report);
        GetFileResponse getFileResponse = mock(GetFileResponse.class);
        File file = mock(File.class);
        when(getFileResponse.file()).thenReturn(file);
        AtomicReference<SendMessage> atomicReference = new AtomicReference<>();
        when(telegramBot.execute(any())).thenAnswer(invocationOnMock -> {
            Object sendRequest = invocationOnMock.getArgument(0);
            if (sendRequest instanceof GetFile) {
                return getFileResponse;
            } else if (sendRequest instanceof SendMessage) {
                atomicReference.set((SendMessage) sendRequest);
                return null;
            }
            return null;
        });
        var actual = out.processMessage(update.message());
        //Then
        Assertions.assertThat(actual.isEmpty()).isFalse();
        Assertions.assertThat(actual.get(0).getParameters().get("text")).isEqualTo("Данные отчета записаны");
    }

    @Test
    void processMessage_UserFirstReport() throws Exception {
        //Given
        Update update = BotListenerUtil.generateUpdate("ok");
        Request request = new Request(update.message().chat().id());
        request.setReportBehaviorRequested(true);
        Report report = new Report();
        report.setId(1L);
        //When
        when(requestsBuffer.getRequest(any())).thenReturn(Optional.of(request));
        when(reportService.findFirstByUserIdAndDate(any(), any())).thenReturn(new Report());
        when(reportService.create(any())).thenReturn(report);
        var actual = out.processMessage(update.message());
        //Then
        Assertions.assertThat(actual.isEmpty()).isFalse();
        Assertions.assertThat(actual.get(0).getParameters().get("chat_id")).isEqualTo(update.message().chat().id());
        Assertions.assertThat(actual.get(0).getParameters().get("text")).isEqualTo("Данные отчета записаны");
    }

    @Test
    void processMessage_ReportSaveWithException() throws Exception {
        //Given
        Update update = BotListenerUtil.generateUpdate("ok");
        Request request = new Request(update.message().chat().id());
        request.setReportBehaviorRequested(true);
        Report report = new Report();
        report.setId(1L);
        //When
        when(requestsBuffer.getRequest(any())).thenReturn(Optional.of(request));
        when(reportService.findFirstByUserIdAndDate(any(), any())).thenReturn(report);
        when(reportService.update(any(), any())).thenThrow(new InvalidDataException("error"));
        var actual = out.processMessage(update.message());
        //Then
        Assertions.assertThat(actual.isEmpty()).isFalse();
        Assertions.assertThat(actual.get(0).getParameters().get("chat_id")).isEqualTo(update.message().chat().id());
        Assertions.assertThat(actual.get(0).getParameters().get("text")).isEqualTo("Возникла ошибка: error");
    }

    @Test
    void processMessage_SendQuestionToVolunteer() throws Exception {
        //Given
        Update update = BotListenerUtil.generateUpdate("question");
        Question question = new Question(update.message().chat().id(), 12L);
        //When
        when(questionsBuffer.getQuestionByUserChat(update.message().chat().id())).thenReturn(Optional.of(question));
        var actual = out.processMessage(update.message());
        //Then
        Assertions.assertThat(actual.isEmpty()).isFalse();
        Assertions.assertThat(actual.size()).isEqualTo(2);
        Assertions.assertThat(actual.get(0).getParameters().get("chat_id")).isEqualTo(12L);
        Assertions.assertThat(actual.get(0).getParameters().get("text")).isEqualTo("1: Сообщение от пользователя, для ответа используйте reply:\n question");
        Assertions.assertThat(actual.get(1).getParameters().get("chat_id")).isEqualTo(11L);
        Assertions.assertThat(actual.get(1).getParameters().get("text")).isEqualTo("Сообщение отправлено волонтеру");
    }

    @Test
    void processMessage_SendReplyFromVolunteer() throws Exception {
        //Given
        String replyMessage = "1: Сообщение от пользователя: вопрос";
        Update update = BotListenerUtil.generateUpdateWithReply(replyMessage);
        Question question = new Question(11L, 12L);
        question.setId(1);
        //When
        when(questionsBuffer.getQuestionById(any())).thenReturn(Optional.of(question));
        var actual = out.processMessage(update.message());
        //Then
        Assertions.assertThat(actual.isEmpty()).isFalse();
        Assertions.assertThat(actual.get(0).getParameters().get("chat_id")).isEqualTo(update.message().chat().id());
        Assertions.assertThat(actual.get(0).getParameters().get("text")).isEqualTo("Ответ волонтера: \n" + "ответ");
    }

    @Test
    void processMessage_UserFirstMessage() throws Exception {
        //Given
        Update update = BotListenerUtil.generateUpdate("/start");
        //When
        when(userService.findByTelegramId(any())).thenReturn(new User());
        when(userService.create(any())).thenReturn(BotListenerUtil.mockUser());
        var actual = out.processMessage(update.message());
        //Then
        Assertions.assertThat(actual.isEmpty()).isFalse();
        Assertions.assertThat(actual.get(0).getParameters().get("chat_id")).isEqualTo(update.message().chat().id());
        Assertions.assertThat(actual.get(0).getParameters().get("text")).isEqualTo("Привет! Для продолжения работы выберите приют.");
    }

    @Test
    void processMessage_WithoutResponse() throws Exception {
        //Given
        Update update = BotListenerUtil.generateUpdate("zzz");
        //When
        var actual = out.processMessage(update.message());
        //Then
        Assertions.assertThat(actual.isEmpty()).isTrue();
    }
}