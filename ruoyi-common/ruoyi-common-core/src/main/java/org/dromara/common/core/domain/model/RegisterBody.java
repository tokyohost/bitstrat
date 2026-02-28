package org.dromara.common.core.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
 * 用户注册对象
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegisterBody extends LoginBody {

    /**
     * 用户名
     */
//    @NotBlank(message = "{user.username.not.blank}")
//    @Length(min = 2, max = 20, message = "{user.username.length.valid}")
    private String username;
    /**
     * email
     */
    @NotBlank(message = "{user.email.not.blank}")
    @Length(min = 2, max = 30, message = "{user.email.not.blank}")
    private String email;

    @NotBlank(message = "{user.emailcode.not.blank}")
    private String emailCode;

    /**
     * 用户密码
     */
    @NotBlank(message = "{user.password.not.blank}")
    @Length(min = 5, max = 20, message = "{user.password.length.valid}")
    private String password;

    /**
     * 邀请码
     */
    private String invitationCode;

    private String userType;

}
