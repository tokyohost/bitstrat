package com.bitstrat.constant;

/**
 * @author caoyang
 * @date 验证vip购买资格
 */

public class CheckPurchaseVipConstant {
    // 用户无会员 或 会员已过期,可正常购买
    public static final int USER_VIP_CAN_PURCHASE = 0;
    // 购买的会员等级低于当前未过期的会员等级,不可购买
    public static final int USER_VIP_CANT_PURCHASE = 1;
    // 购买的会员等级等于当前未过期的会员等级,续费
    public static final int USER_VIP_RENEW = 2;
    // 购买的会员等级大于当前未过期的会员等级,升级
    public static final int USER_VIP_UPGRADE = 3;
}
