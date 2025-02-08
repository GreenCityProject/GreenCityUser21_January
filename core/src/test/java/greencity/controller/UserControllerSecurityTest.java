package greencity.controller;

import greencity.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.user.UserStatusDto;
import greencity.enums.UserStatus;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UserController.class, SecurityConfig.class})
@WebMvcTest(controllers = UserController.class)
public class UserControllerSecurityTest {
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
    void findUserForManagementByPageForbiddenTest() throws Exception {
        mvc.perform(get(userLink + "/findUserForManagement")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void updateStatusForbiddenTest() throws Exception {
        UserStatusDto userStatusDto = new UserStatusDto();
        mvc.perform(patch(userLink + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userStatusDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = "ADMIN")
    void findUserForManagementByPageOkTest() throws Exception {
        mvc.perform(get(userLink + "/findUserForManagement")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = "ADMIN")
    void updateStatusOkTest() throws Exception {
        UserStatusDto userStatusDto = new UserStatusDto();
        userStatusDto.setId(6L);
        userStatusDto.setUserStatus(UserStatus.CREATED);

        when(userService.updateStatus(userStatusDto.getId(), userStatusDto.getUserStatus(), "user"))
                .thenReturn(userStatusDto);

        mvc.perform(patch(userLink + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userStatusDto)))
                .andExpect(status().isOk());
    }
}
