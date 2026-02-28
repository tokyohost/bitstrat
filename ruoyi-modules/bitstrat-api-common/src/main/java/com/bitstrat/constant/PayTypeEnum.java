package com.bitstrat.constant;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/14 15:23
 * @Content
 */

public enum PayTypeEnum {
    ALIPAY("alipay","alipay"),
    STRIPE("stripe","stripe");

    private String type;
    private String desc;

    PayTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static PayTypeEnum getPayTypeEnum(String type){
        for(PayTypeEnum payTypeEnum : PayTypeEnum.values()){
            if(payTypeEnum.getType().equals(type)){
                return payTypeEnum;
            }
        }
        return null;
    }
}

