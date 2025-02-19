package greencity.security.service;

import greencity.dto.user.UserVO;

public interface GoogleSecurityService {
    /**
     * Creates a new user via Google OAuth2.
     *
     * @param email    User's email.
     * @param name     User's name.
     * @param picture  URL to the user's avatar.
     * @param language Preferred language of the user.
     * @return UserVO of the created user.
     */

    UserVO createNewGoogleUser(String email, String name, String picture, String language);

    UserVO findOrCreateUserByEmail(String email);
}