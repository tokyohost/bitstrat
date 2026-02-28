package com.bitstrat.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/11 21:51
 * @Content
 */
@Data
public class ConnectSocketMutiBody {
    List<ExchangeAccountVo> exchanges;
}
