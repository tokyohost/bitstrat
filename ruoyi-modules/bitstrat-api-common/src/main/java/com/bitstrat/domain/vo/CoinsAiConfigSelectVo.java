package com.bitstrat.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.CoinsAiConfig;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;


/**
 * ai 流水线配置视图对象 coins_ai_config
 *
 * @author Lion Li
 * @date 2025-04-01
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsAiConfig.class)
public class CoinsAiConfigSelectVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 流水线名称
     */
    @ExcelProperty(value = "流水线名称")
    private String flowName;

    /**
     * 对象转封装类
     *
     * @param coinsAiConfig CoinsAiConfig实体对象
     * @return CoinsAiConfigVo
     */
    public static CoinsAiConfigSelectVo objToVo(CoinsAiConfigVo coinsAiConfig) {
        if (coinsAiConfig == null) {
            return null;
        }
        CoinsAiConfigSelectVo coinsAiConfigVo = new CoinsAiConfigSelectVo();
        BeanUtils.copyProperties(coinsAiConfig, coinsAiConfigVo);
        return coinsAiConfigVo;
    }

}
