package greencity.security.service;

import greencity.dto.user.UserVO;
import greencity.entity.Language;
import greencity.entity.User;
import greencity.enums.EmailNotification;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.repository.UserRepo;
import greencity.security.jwt.JwtTool;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleSecurityServiceImpl implements GoogleSecurityService {
    private static final Double DEFAULT_RATING = 0.0;
    private final UserRepo userRepo;
    private final ModelMapper modelMapper;
    private final JwtTool jwtTool;

    @Override
    public UserVO createNewGoogleUser(String email, String name, String picture, String language) {
        Optional<User> existingUser = userRepo.findByEmail(email);
        if (existingUser.isPresent()) {
            return modelMapper.map(existingUser.get(), UserVO.class);
        }

        User newUser = createNewUser(email, name, picture, language);

        User savedUser = userRepo.save(newUser);

        return modelMapper.map(savedUser, UserVO.class);
    }

    private User createNewUser(String email, String name, String picture, String language) {
        return User.builder()
                .email(email)
                .name(name)
                .role(Role.ROLE_USER)
                .uuid(UUID.randomUUID().toString())
                .rating(DEFAULT_RATING)
                .profilePicturePath(picture)
                .dateOfRegistration(LocalDateTime.now())
                .lastActivityTime(LocalDateTime.now())
                .userStatus(UserStatus.ACTIVATED)
                .emailNotification(EmailNotification.DISABLED)
                .language(Language.builder()
                        .id(modelMapper.map(language, Long.class))
                        .build())
                .refreshTokenKey(jwtTool.generateTokenKey())
                .build();
    }

    public UserVO findOrCreateUserByEmail(String email) {
        Optional<User> existingUser = userRepo.findByEmail(email);
        return existingUser.map(user -> modelMapper.map(user, UserVO.class))
                .orElse(null);
    }

}