package org.dromara.common.core.exception.coins;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dromara.common.core.exception.base.BaseException;

import java.io.Serial;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/12 19:07
 * @Content
 */
public class CoinsException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CoinsException(String code, Object... args) {
        super("coins", code, args, null);
    }
}
