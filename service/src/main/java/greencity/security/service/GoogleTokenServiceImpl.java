package greencity.security.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import greencity.security.service.GoogleTokenService;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class GoogleTokenServiceImpl implements GoogleTokenService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final HttpSession session;

    public GoogleTokenServiceImpl(OAuth2AuthorizedClientService authorizedClientService, HttpSession session) {
        this.authorizedClientService = authorizedClientService;
        this.session = session;
    }

    @Override
    public String getAccessToken(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            return null;
        }

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        System.out.print("OAuth2User from getAccessToken of GoogleTokenService: ");
        System.out.println(oauth2User.toString());
        String clientRegistrationId = "google";

        OAuth2AuthorizedClient authorizedClient =
                authorizedClientService.loadAuthorizedClient(clientRegistrationId, oauth2User.getName());

        if (authorizedClient != null) {
            String token = authorizedClient.getAccessToken().getTokenValue();

            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    authentication.getPrincipal(), token, authentication.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            session.setAttribute("ACCESS_TOKEN", token);

            return token;
        } else {
            log.warn("No authorized client found for user: {}", oauth2User.getName());
        }

        return null;
    }

    public String getEmailFromToken(String token) {
        try {
            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = new GsonFactory();

            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
            GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token);

            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse response = request.execute();

            Map<String, Object> userInfo = new Gson().fromJson(response.parseAsString(), Map.class);
            return (String) userInfo.get("email");
        } catch (IOException e) {
            log.error("Error retrieving email from Google token: {}", e.getMessage(), e);
            return null;
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

}
