package com.binance.connector.futures.client.timeSync;

import net.time4j.Moment;
import net.time4j.TemporalType;
import net.time4j.base.TimeSource;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/12 11:16
 * @Content
 */

public class ServerTimeSource implements TimeSource<Moment> {
    private final long offset;

    public ServerTimeSource(long serverTimeUTInMillisecsSince1970) {
        this.offset = serverTimeUTInMillisecsSince1970 - System.currentTimeMillis();
    }

    public Moment currentTime() {
        long timeInMillis = System.currentTimeMillis() + this.offset;
        return TemporalType.MILLIS_SINCE_UNIX.translate(timeInMillis);
    }

}
