package org.dromara.common.core.exception.coins.api;

import org.dromara.common.core.exception.coins.CoinsException;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/12 19:08
 * @Content
 */

public class ApiNotFindException extends CoinsException {
    public ApiNotFindException() {
        super("coins.api.api-not-find");
    }
}
