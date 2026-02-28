package com.bitstrat.constant;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/20 19:22
 * @Content
 * "long" | "short" | "hold"|"nothing" | "tpsl" |"close"|"reduce"
 */

public enum ActionEnum {
    LONG("LONG","做多"),
    SHORT("SHORT","做空"),
    NOTHING("NOTHING","无操作"),
    HOLD("HOLD","无操作"),
    TPSL("TPSL","调整止盈止损"),
    CLOSE("CLOSE","平仓"),
    REDUCE("REDUCE","减仓");

    private String action;
    private String desc;

     ActionEnum(String action, String desc) {
        this.action = action;
        this.desc = desc;
    }

    public String getAction() {
        return action;
    }

    public String getDesc() {
        return desc;
    }
    /**
     * 判断给定的类型是否包含其它类型
     */
    public static boolean contains(String type,ActionEnum...  items){
        if(items==null||items.length==0){
            return false;
        }

        for (ActionEnum item : items) {
            if(item.getAction().equalsIgnoreCase(type)){
                return true;
            }
        }
        return false;
    }
}
