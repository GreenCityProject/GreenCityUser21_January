package greencity.security.service;

import org.springframework.security.core.Authentication;

public interface GoogleTokenService {
    /**
     * Retrieves the Google access token for the authenticated user.
     *
     * @param authentication the authentication object containing user details
     * @return the access token if available, or null if not found
     */
    String getAccessToken(Authentication authentication);

    String getEmailFromToken(String googleToken);
}
