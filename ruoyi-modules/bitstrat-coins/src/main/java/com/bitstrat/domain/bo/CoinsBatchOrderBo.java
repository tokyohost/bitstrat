package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsBatchOrder;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 分批任务订单记录业务对象 coins_batch_order
 *
 * @author Lion Li
 * @date 2025-04-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsBatchOrder.class, reverseConvertGenerate = false)
public class CoinsBatchOrderBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 批次id
     */
    private Long batchId;

    /**
     * 当前执行批次
     */
    private Long currBatch;

    /**
     * 下单数量
     */
    private Long orderSize;

    /**
     * 状态 10-已下单 20-已成交 30-异常
     */
    private Long status;

    /**
     * 异常信息
     */
    private String msg;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 用户id
     */
    @NotNull(message = "用户id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userId;


}
