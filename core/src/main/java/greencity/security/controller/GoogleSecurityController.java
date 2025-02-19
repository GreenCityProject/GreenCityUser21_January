package greencity.security.controller;

import greencity.dto.user.UserVO;
import greencity.security.dto.SuccessSignInDto;
import greencity.security.jwt.JwtTool;
import greencity.security.service.GoogleSecurityService;
import greencity.security.service.GoogleTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling OAuth2 authentication success and failure events.
 * Provides endpoints to process authenticated user data and log any errors.
 */
@RestController
@RequestMapping("/oauth2")
public class GoogleSecurityController {
    private final GoogleTokenService googleTokenService;
    private final GoogleSecurityService googleAuthService;
    private final JwtTool jwtTool;

    @Autowired
    public GoogleSecurityController(GoogleTokenService googleTokenService, GoogleSecurityService googleAuthService, JwtTool jwtTool) {
        this.googleTokenService = googleTokenService;
        this.googleAuthService = googleAuthService;
        this.jwtTool = jwtTool;
    }

    /**
     * Handles OAuth2 authentication success, creating a new user if needed.
     */
    @GetMapping("/success")
    public ResponseEntity<String> oauthSuccess(Authentication authentication, @RequestParam(defaultValue = "en") String language) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed.");
        }

        try {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String picture = oauth2User.getAttribute("picture");

            if (email == null || name == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing required user information.");
            }

            googleAuthService.createNewGoogleUser(email, name, picture, language);
            return ResponseEntity.ok("User " + name + " successfully signed in!");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during authentication.");
        }
    }

    /**
     * Endpoint to retrieve Google OAuth2 access token.
     */
    @GetMapping("/token")
    public ResponseEntity<String> getGoogleToken(Authentication authentication) {
        String token = googleTokenService.getAccessToken(authentication);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No token found.");
        }
        return ResponseEntity.ok(token);
    }

    /**
     * Authenticates user via Google access token.
     *
     * @param googleToken Google OAuth2 access token (required).
     * @return SuccessSignInDto if authentication is successful, or 400 if the token is invalid.
     */
    @GetMapping("/authenticate")
    public ResponseEntity<SuccessSignInDto> authenticate(@RequestParam("googleToken") String googleToken) {

        String email = googleTokenService.getEmailFromToken(googleToken);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        UserVO userVO = googleAuthService.findOrCreateUserByEmail(email);

        String accessToken = jwtTool.createAccessToken(userVO.getEmail(), userVO.getRole());
        String refreshToken = jwtTool.createRefreshToken(userVO);

        SuccessSignInDto response = SuccessSignInDto.builder()
                .userId(userVO.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .name(userVO.getName())
                .ownRegistrations(false)
                .build();

        return ResponseEntity.ok(response);
    }


}