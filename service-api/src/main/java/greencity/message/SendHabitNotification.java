package greencity.message;

import java.io.Serializable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Message, that is used for sending emails about not marked habits.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SendHabitNotification implements Serializable {

    @NotBlank(message = "Name must not be blank")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Name must contain only alphabetic characters and spaces")
    private String name;

    @Email(message = "Email must have a correct format")
    @NotBlank(message = "Email must not be blank")
    private String email;
}
