package com.bitstrat.okx.model;

import com.bitstrat.okx.WebSocketHandler;
import lombok.Data;

import java.util.List;

/**
 * {
 *     "op":"subscribe",
 *     "args":[
 *         {
 *             "channel":"mark-price",
 *             "instId":"PROMPT-USDT-SWAP"
 *         }
 *     ]
 * }
 *
 * {
 *     "op":"unsubscribe",
 *     "args":[
 *         {
 *             "channel":"mark-price",
 *             "instId":"PROMPT-USDT-SWAP"
 *         }
 *     ]
 * }
 */
@Data
public class OkxSubscriptMsg {
    private String op;
    private List<SubscriptArg> args;

}
