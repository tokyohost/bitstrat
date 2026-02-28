package com.bitstrat.domain;

import com.bitstrat.domain.vo.CoinsApiVo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/11/25 10:50
 * @Content
 */

@Data
@AutoMapper(target = CoinsApiVo.class)
public class ApiVO {
    private Long id;
    private String name;
    private String exchangeName;
    private String type;
}
