package greencity.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import greencity.dto.econews.EcoNewsForSendEmailDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.user.UserVO;
import greencity.dto.violation.UserViolationMailDto;
import greencity.message.SendHabitNotification;
import greencity.message.SendReportEmailMessage;
import greencity.service.EmailService;
import greencity.service.UserService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.security.Principal;
import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailControllerTest {
    private static final String LINK = "/email";
    private MockMvc mockMvc;

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @InjectMocks
    private EmailController emailController;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(emailController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void addEcoNews() throws Exception {
        String content =
            "{\"unsubscribeToken\":\"string\"," +
                "\"creationDate\":\"2021-02-05T15:10:22.434Z\"," +
                "\"imagePath\":\"string\"," +
                "\"source\":\"string\"," +
                "\"author\":{\"id\":0,\"name\":\"string\",\"email\":\"test.email@gmail.com\" }," +
                "\"title\":\"string\"," +
                "\"text\":\"string\"}";

        mockPerform(content, "/addEcoNews");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        EcoNewsForSendEmailDto message = objectMapper.readValue(content, EcoNewsForSendEmailDto.class);

        verify(emailService).sendCreatedNewsForAuthor(message);
    }

    @Test
    void sendReport() throws Exception {
        String content = "{" +
            "\"categoriesDtoWithPlacesDtoMap\":" +
            "{\"additionalProp1\":" +
            "[{\"category\":{\"name\":\"string\",\"parentCategoryId\":0}," +
            "\"name\":\"string\"}]," +
            "\"additionalProp2\":" +
            "[{\"category\":{\"name\":\"string\",\"parentCategoryId\":0}," +
            "\"name\":\"string\"}]," +
            "\"additionalProp3\":[{\"category\":{\"name\":\"string\",\"parentCategoryId\":0}," +
            "\"name\":\"string\"}]}," +
            "\"emailNotification\":\"string\"," +
            "\"subscribers\":[{\"email\":\"string\",\"id\":0,\"name\":\"string\"}]}";

        mockPerform(content, "/sendReport");

        SendReportEmailMessage message =
            new ObjectMapper().readValue(content, SendReportEmailMessage.class);

        verify(emailService).sendAddedNewPlacesReportEmail(
            message.getSubscribers(), message.getCategoriesDtoWithPlacesDtoMap(),
            message.getEmailNotification());
    }

    @ParameterizedTest
    @CsvSource({
            "Admin1@gmail.com, Joe, 'Some place', unregistered, 200",
            "'', '', '', '', 400",
            "Admin1gmail.com, Joe, 'Some place', unregistered, 400",
            "Admin1@gmail.com, joe, 'Some place', '', 400",
            "Admin1@gmail.com, Joe, 'some place', '', 400",
            "Admin1@gmail..com, joe, 'Some place', '', 400",
            "Admin1@gmail.com, Joe, 'Some place', unregisteredunregisteredunregistered, 400",
            "Admin1@gmail.com, J, 'Some place', unregisteredunregisteredunregistered, 400",
            "Admin1@gmail.com, Joe, s, unregistered, 400",
            "Admin1@gmail.com, '', 'Some-place', unregistered, 400",
            "Admin1@gmail.com, Joe, '', unregistered, 400",
            "'', Joe, 'Some place', unregistered, 400",
    })
    void testChangePlaceStatusReturns200_Or_400(String email, String authorName, String placeName,
                                     String placeStatus, int statusCode) throws Exception {

        String content = String.format("{" +
            "\"authorEmail\":\"%s\"," +
            "\"authorFirstName\":\"%s\"," +
            "\"placeName\":\"%s\"," +
            "\"placeStatus\":\"%s\"" +
            "}", email, authorName, placeName, placeStatus);

        if(statusCode == 200)
            doNothing().when(emailService).sendChangePlaceStatusEmail(authorName, placeName, placeStatus, email);

        mockMvc.perform(post(LINK+ "/changePlaceStatus").contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().is(statusCode))
                .andDo(print());

        if(statusCode==200)
            verify(emailService, times(1)).sendChangePlaceStatusEmail(authorName, placeName,
                placeStatus, email);
        else
            verify(emailService, times(0)).sendChangePlaceStatusEmail(authorName, placeName,
                    placeStatus, email);
    }

    @Test
    void sendHabitNotification() throws Exception {
        String content = "{" +
            "\"email\":\"string\"," +
            "\"name\":\"string\"" +
            "}";

        mockPerform(content, "/sendHabitNotification");

        SendHabitNotification notification =
            new ObjectMapper().readValue(content, SendHabitNotification.class);

        verify(emailService).sendHabitNotification(notification.getName(), notification.getEmail());
    }

    @ParameterizedTest
    @CsvSource({
            "Joe Doe, Test1@gmail.com, 200",
            "'','',400",
            "1111, validemail@gmail.com, 400",
             "Joe, Test1@mail..com, 400",
            "1111, Test1mail.com, 400",
            "'', Test1@gmail.com, 400",
            "Joe Doe, '', 400"
    })
    void sendHabitNotification(String name, String email, int expectedStatus) throws Exception {

        Principal principal = ()->"test@test.com";

        String requestBody = String.format("{\"name\":\"%s\",\"email\":\"%s\"}", name, email);

        if(expectedStatus ==200) {
            when(userService.findByEmail("Test1@gmail.com")).thenReturn(new UserVO());
            doNothing().when(emailService).sendHabitNotification(name, email);
        }

        mockMvc.perform(post(LINK + "/sendHabitNotification")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().is(expectedStatus))
                .andDo(print());

        if(expectedStatus == 200)
            verify(emailService, times(1)).sendHabitNotification(name, email);
        else
            verify(emailService, times(0)).sendHabitNotification(anyString(), anyString());
    }

    @ParameterizedTest
    @CsvSource({
            "Joe Doe, Test1@gmail.com, 200",
            "'','',400",
            "1111, validemail@gmail.com, 400",
            "Joe, Test1@mail..com, 400",
            "1111, Test1mail.com, 400",
            "'', Test1@gmail.com, 400",
            "Joe Doe, '', 400"
    })
    void testSendHabitNotificationStatuses200And400(String name, String email, int expectedStatus) throws Exception {

        Principal principal = ()->"test@test.com";

        String requestBody = String.format("{\"name\":\"%s\",\"email\":\"%s\"}", name, email);

        if(expectedStatus ==200) {
            when(userService.findByEmail("Test1@gmail.com")).thenReturn(new UserVO());
            doNothing().when(emailService).sendHabitNotification(name, email);
        }

        mockMvc.perform(post(LINK + "/sendHabitNotification")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().is(expectedStatus))
                .andDo(print());

        if(expectedStatus == 200)
            verify(emailService, times(1)).sendHabitNotification(name, email);
        else
            verify(emailService, times(0)).sendHabitNotification(anyString(), anyString());
    }


    private void mockPerform(String content, String subLink) throws Exception {
        mockMvc.perform(post(LINK + subLink)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content))
            .andExpect(status().isOk());
    }



    @Test
    void sendUserViolationEmailTest() throws Exception {
        String content = "{" +
            "\"name\":\"String\"," +
            "\"email\":\"String@gmail.com\"," +
            "\"violationDescription\":\"string string\"" +
            "}";

        mockPerform(content, "/sendUserViolation");

        UserViolationMailDto userViolationMailDto = new ObjectMapper().readValue(content, UserViolationMailDto.class);
        verify(emailService).sendUserViolationEmail(userViolationMailDto);
    }

    @Test
    @SneakyThrows
    void sendUserNotification() {
        String content = "{" +
            "\"title\":\"title\"," +
            "\"body\":\"body\"" +
            "}";
        String email = "email@mail.com";

        mockMvc.perform(post(LINK + "/notification")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
            .param("email", email))
            .andExpect(status().isOk());

        NotificationDto notification = new ObjectMapper().readValue(content, NotificationDto.class);
        verify(emailService).sendNotificationByEmail(notification, email);
    }
}
