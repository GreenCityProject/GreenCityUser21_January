package greencity.message;

import java.io.Serializable;

import greencity.constant.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public final class SendChangePlaceStatusEmailMessage implements Serializable {

    @NotBlank(message = "Author name should not be empty")
    @Pattern(regexp = ValidationConstants.USERNAME_REGEXP, message = ValidationConstants.USERNAME_MESSAGE)
    private String authorFirstName;

    @NotBlank(message = "Place name should not be empty")
    @Pattern(regexp = ValidationConstants.USERNAME_REGEXP,
            message = "Place name must contain valid characters and spaces")
    private String placeName;

    @NotBlank(message = "Place name should not be empty")
    @Size(min = 1, max = ValidationConstants.PLACE_NAME_MAX_LENGTH)
    private String placeStatus;

    @Email(message = ValidationConstants.INVALID_EMAIL)
    @NotBlank(message = "Email is a required field!")
    private String authorEmail;
}
