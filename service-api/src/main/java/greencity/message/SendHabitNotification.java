package greencity.message;

import java.io.Serializable;

import jakarta.validation.constraints.*;
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
    @Pattern(regexp = "^[A-Za-z\\s-'`]+$", message = "Name must contain valid characters and spaces")
    private String name;

    @Email(message = "Email must have a correct format")
    @NotBlank(message = "Email must not be blank")
    private String email;
}
