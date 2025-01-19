package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import greencity.config.SecurityConfig;
import greencity.dto.language.LanguageVO;
import greencity.dto.ownsecurity.OwnSecurityVO;
import greencity.dto.user.UserManagementUpdateDto;
import greencity.dto.user.UserUpdateDto;
import greencity.dto.user.UserVO;
import greencity.dto.verifyemail.VerifyEmailVO;
import greencity.enums.EmailNotification;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.security.jwt.JwtTool;
import greencity.service.EmailService;
import greencity.service.UserService;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    void getUserByPrincipalOkTest() throws Exception {
        mvc.perform(get(userLink))
                .andExpect(status().isOk());
    }

    @Test
    void getUserByPrincipal401Test() throws Exception {
        mvc.perform(get(userLink))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = "ADMIN")
    void saveUserOkTest() throws Exception {
        UserVO user = new UserVO();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john.doe@gmail.com");
        user.setRole(Role.ROLE_ADMIN);

        VerifyEmailVO verifyEmail = new VerifyEmailVO();
        verifyEmail.setId(1L);
        verifyEmail.setUser(user);
        verifyEmail.setToken("f570b374-ebba-4242-bc21-93f30c05113a");

        OwnSecurityVO ownSecurity = new OwnSecurityVO();
        ownSecurity.setId(1L);
        ownSecurity.setPassword("password");
        ownSecurity.setUser(user);

        LanguageVO language = new LanguageVO();
        language.setId(1L);
        language.setCode("en");

        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setName("John");
        userVO.setEmail("john.doe@gmail.com");
        userVO.setUserCredo("Live and let live");
        userVO.setUserStatus(UserStatus.CREATED);
        userVO.setVerifyEmail(verifyEmail);
        userVO.setRating(4.5);
        userVO.setEmailNotification(EmailNotification.DISABLED);
        userVO.setRefreshTokenKey("f570b374-ebba-4242-bc21-93f30c05113a");
        userVO.setOwnSecurity(ownSecurity);
        userVO.setProfilePicturePath("/images/profile_picture.png");
        userVO.setCity("Kharkiv");
        userVO.setShowLocation(true);
        userVO.setShowEcoPlace(true);
        userVO.setShowShoppingList(true);
        userVO.setLanguageVO(language);
        userVO.setRole(Role.ROLE_ADMIN);

        when(userService.save(userVO)).thenReturn(userVO);

        String json = new ObjectMapper().writeValueAsString(user);
        System.out.println(json);

        MvcResult result = mvc.perform(post(userLink)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userVO)))
                .andExpect(status().isOk()).andReturn();

        Assertions.assertEquals("Kharkiv", JsonPath.read(result.getResponse().getContentAsString(), "$.city"));
        verify(userService, times(1)).save((userVO));
    }

    @Test
    @WithMockUser(username = "user", roles = "ADMIN")
    void updateUser200Test() throws Exception {
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setName("Anna");
        userUpdateDto.setEmailNotification(EmailNotification.DISABLED);

        mvc.perform(patch(userLink)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = "ADMIN")
    void updateUserManagement200Test() throws Exception {
        long id = 1L;

        UserManagementUpdateDto userManagementUpdateDto = new UserManagementUpdateDto();
        userManagementUpdateDto.setUserCredo("I believe in unicorns");
        userManagementUpdateDto.setUserStatus(UserStatus.CREATED);
        userManagementUpdateDto.setEmail("testemail@gmail.com");
        userManagementUpdateDto.setName("Anna");
        userManagementUpdateDto.setRole(Role.ROLE_USER);

        mvc.perform(put(userLink + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userManagementUpdateDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = "ADMIN")
    void updateRole200Test() throws Exception {
        long id = 1L;

        Map<String, String> map = new HashMap<>();
        map.put("role", "ROLE_USER");

        mvc.perform(patch(userLink + "/" + id + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(map)))
                .andExpect(status().isOk());
    }
}
