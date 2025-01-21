package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.config.SecurityConfig;
import greencity.dto.user.UserStatusDto;
import greencity.security.jwt.JwtTool;
import greencity.service.EmailService;
import greencity.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UserController.class, SecurityConfig.class})
@WebMvcTest(controllers = UserController.class)
public class CustomUserControllerTest {

    private static final String userLink = "/user";
    @Autowired
    private MockMvc mvc;
    @MockBean
    private UserService userService;
    @MockBean
    private EmailService emailService;
    @MockBean
    private JwtTool jwtTool;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void updateUserLastActivityDate_ForbiddenTest() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.now();

        UserStatusDto userStatusDto = new UserStatusDto();
        mvc.perform(put(userLink + "/updateUserLastActivityTime/" + localDateTime)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userStatusDto)))
                .andExpect(status().isForbidden());
    }

}
