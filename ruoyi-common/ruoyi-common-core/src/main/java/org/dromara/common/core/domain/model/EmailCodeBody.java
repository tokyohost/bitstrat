package org.dromara.common.core.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class EmailCodeBody {

    /**
     * 邀请码
     */
//    @NotBlank(message = "{user.invitationCodeNotEmpty}")
    private String invitationCode;

    @NotBlank(message = "{user.email.not.blank}")
    @Length(min = 2, max = 30, message = "{user.email.not.blank}")
    private String email;

    @NotEmpty(message = "UUID ERROR")
    private String uuid;

    @NotEmpty(message = "code cannot be empty")
    private String code;

}
