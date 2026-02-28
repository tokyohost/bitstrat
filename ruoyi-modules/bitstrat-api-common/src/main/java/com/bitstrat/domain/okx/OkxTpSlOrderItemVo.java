package com.bitstrat.domain.okx;

import com.bitstrat.domain.TpSlOrder;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/8 9:16
 * @Content
 */

@Data
@AutoMapper(target = OkxTpSlOrderItem.class,reverseConvertGenerate = true)
public class OkxTpSlOrderItemVo extends TpSlOrder {

    private String tpTriggerPx;
    private String slTriggerPx;
    private String slOrdPx;
    private String tpOrdPx;
    private String sz;
    private String side;
    private String reduceOnly;
    private String lever;
}
